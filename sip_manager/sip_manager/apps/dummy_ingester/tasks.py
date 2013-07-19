"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)



"""

import gzip
import time
import os
import threading
from datetime import datetime, timedelta

from django.db import connection

from django.core import exceptions
from django.core.mail import send_mail
from django.conf import settings

from apps.sipmanager import sip_task
from apps.sipmanager import models as sipm_mods
from apps.base_item import models as base_item
from apps.plug_uris import models as uri_models
from apps.plug_uris.update_req_stats import UpdateRequestStats

from utils.gen_utils import calculate_hash

import models


REC_START = '<record>'
REC_STOP = '</record>'

LAST_INGEST_EXAMINATION = 0


REQUEST_CREATE_FIRST_RUN = True


PATH_COLLECTIONS = settings.PATH_COLLECTIONS

WATCHDOG_LAST_RUN = 0


class Watchdog(sip_task.SipTask):
    """
    Ensures no process have been idle to long, will send mail if any found.
    """
    SHORT_DESCRIPTION = 'watchdog'
    PRIORITY = sip_task.SIP_PRIO_HIGH
    
    def prepare(self):
        global WATCHDOG_LAST_RUN
        
        if not WATCHDOG_LAST_RUN:
            # starting up send initial msg to ensure setup is ok
            self.log('Mailing admins Watchdog is starting', 3)
            for eadr in settings.ADMIN_EMAILS:
                send_mail('Thumbler notification', 'Watchdog is starting.', '', [eadr], fail_silently=False)
            
        if WATCHDOG_LAST_RUN + 300 > time.time():
            # only check and potentionally mail out every five mins
            return False
        WATCHDOG_LAST_RUN = time.time()
        return True
        
        
    def run_it(self):
        global WATCHDOG_LAST_RUN
        self.log('Watchdog checking for stale procs', 8)
        cut_of_time = datetime.now() - timedelta(hours=1)
        stale_procs = sipm_mods.ProcessMonitoring.objects.filter(last_change__lt = cut_of_time)
        proc_count = len(stale_procs)
        if not proc_count:
            return False

        WATCHDOG_LAST_RUN = time.time() + 3600 # only send notification once/hour
        msg = 'Watchdog detected (%i) stale processes' % proc_count
        self.log(msg , 1)
        for eadr in settings.ADMIN_EMAILS:
            send_mail('Thumbler warning!', msg, '', [eadr], fail_silently=False)
        
    
    


class NewRequstCreate(sip_task.SipTask):
    SHORT_DESCRIPTION = 'Checking reqlist for new requests'
    THREAD_MODE = sip_task.SIPT_SINGLE
    PRIORITY = sip_task.SIP_PRIO_HIGH
    
    def __init__(self, *args, **kwargs):
        if not PATH_COLLECTIONS:
            raise exceptions.ImproperlyConfigured('Missing setting PATH_COLLECTIONS - see local_settings_sample.py')
        super(NewRequstCreate, self).__init__(*args, **kwargs)
        self.default_task_show_log_lvl = self.task_show_log_lvl
        self.new_requests = []

    def prepare(self):
        reqlst = models.ReqList.objects.filter(processing=True)
        reqlst.update() # needed to avoid caching and catch external changes
        for rl in reqlst:
            if not models.Request.objects.filter(pk_reqlist=rl.pk):
                # found a new request to process
                self.new_requests.append(rl)
        if self.new_requests:
            self.initial_message = 'found %i new ingestion files' % len(self.new_requests)
        return self.new_requests

    def run_it(self):
        for rl in self.new_requests:
            file_name = os.path.split(rl.rel_path)[1]
            full_path = os.path.join(PATH_COLLECTIONS, rl.rel_path)
            mtime = os.path.getmtime(full_path)
            time_created = datetime.fromtimestamp(mtime)
            
            r = models.Request(pk_reqlist=rl.pk,
                               file_name=file_name, rel_path=rl.rel_path,
                               record_count=rl.rec_count,
                               time_created=time_created)
            r.save()
            
            rl.last_processed = datetime.now()
            rl.save()
            self.task_time_to_show('Added request %s' % file_name,
                                   terminate_on_high_load=True)
        return True

    
class RequestParseNew(sip_task.SipTask):
    SHORT_DESCRIPTION = 'Parse new Requests'
    PLUGIN_TAXES_CPU = True
    THREAD_MODE = sip_task.SIPT_THREADABLE
    PRIORITY = sip_task.SIP_PRIO_INCREASED

    INSTANCES = 3

    def prepare(self):
        try:
            # if the slice fails just report nothing to do
            request = models.Request.objects.filter(status=models.REQS_PRE,
                                                        pid=0)[0]
        except:
            return False

        # in order not to grab control to long, just handle one request on each call to this
        self.request_id = request.id
        self.initial_message = request.file_name
        return True


    def run_it(self):
        request = self.grab_item(models.Request, self.request_id,
                                 'About to parse for ese records')
        if not request:
            return False # Failed to take control of it
        request.status = models.REQS_INIT
        request.save()

        self.current_request = request # save it for later use
        if not self.verify_file():
            # verify has already logged the error, so just exit
            return False

        self.log('Parsing ese file for records: %s' % self.current_request.rel_path, 1)
        self.task_starting('Reading ESE records from file %s (req:%i)' % (request.file_name, request.pk),request.record_count)
        record_count = self.find_records(self.current_request.rel_path, self.add_record, request=request)
        if record_count == -1:
            # aborted due to SipSystemOverLoaded, return state so this will be processed again
            request.status = models.REQS_PRE
            b = False
        else:
            request.status = models.REQS_IMPORTED
            request.record_count = record_count # update with actual count
            b = True
        request.save()
        self.release_item(models.Request, request.pk)
        return b


    def find_records(self, file_name, record_action, **action_kwargs):
        full_path = os.path.join(PATH_COLLECTIONS, file_name)
        record_count = 0
        record = []
        self.task_force_progress_timeout()
        if full_path[-3:].lower() == '.gz':
            f = gzip.open(full_path,'rb')
        else:
            f = open(full_path, 'r')
        for raw_line in f:
            line = raw_line[:-1].strip() # skip lf and other pre/post whitespace
            line_lower = line.lower()
            if line_lower == REC_START:
                record = []
            elif line_lower == REC_STOP:
                record_count += 1
                # start and stop tags shouldnt be sorted so add them after
                record.insert(0, REC_START)
                record.append(REC_STOP)
                record_action(record, **action_kwargs)
                # we dont alow this one to terminate on high load, since it would be
                # very expensive to restart this
                # hovewer we might still get an exception if system is terminating
                try:
                    self.task_time_to_show(record_count)
                except sip_task.SipSystemOverLoaded:
                     # Terminate in a controled fashion so we can do cleanup
                    record_count = -1
                    break
            elif line: # skip empty lines
                record.append(line)
        f.close()
        return record_count


    def add_record(self, record, request):
        record_str = '\n'.join(record)
        """
        When calculating the content hash for the record, the following is asumed:
          the lines are stripped for initial and trailing whitespaces,
          sorted alphabetically
          each line is separated by one \n character
          and finaly the <record> and </record> should be kept!
          the <record>,</record> should obviously not be sorted...
        """
        r_hash = calculate_hash(record_str)
        mdr, was_created = base_item.MdRecord.objects.get_or_create(
            content_hash=r_hash, source_data=record_str)

        if was_created:
            # join the mdrecord to its request, if process was aborted earlier
            # this link already exists
            r_m = base_item.RequestMdRecord(request=request.pk, md_record=mdr.pk)
            r_m.save()
        return


    def verify_file(self):
        full_path = os.path.join(PATH_COLLECTIONS, self.current_request.rel_path)
        if not os.path.exists(full_path):
            return self.request_failure('File not found %s' % self.current_request.rel_path)
        mtime = os.path.getmtime(full_path)
        time_created = datetime.fromtimestamp(mtime)
        if str(time_created).find(str(self.current_request.time_created)) != 0:
            return self.request_failure('File was modified: %s' % self.current_request.rel_path)
        return True


    def request_failure(self, msg):
        req_pk = self.current_request.pk
        self.current_request.status = models.REQS_ABORTED
        self.current_request.err_msg = msg
        self.current_request.save()
        self.release_item(models.Request, self.current_request.pk)
        self.current_request = None
        self.error_log('%s [id %i]' % (msg, req_pk))
        return False # propagate error








class RequstCancel(sip_task.SipTask):
    """
    SELECT r.pk_reqlist, rl.id 
    from dummy_ingester_request r, dummy_ingester_reqlist rl
    where rl.name || '.xml' = r.file_name
    order by r.pk_reqlist

    update dummy_ingester_request set pk_reqlist = dummy_ingester_reqlist.id
    FROM dummy_ingester_reqlist 
    where dummy_ingester_reqlist.name || '.xml' = dummy_ingester_request.file_name
    """
    SHORT_DESCRIPTION = 'Canceling requests'
    THREAD_MODE = sip_task.SIPT_SINGLE
    PRIORITY = sip_task.SIP_PRIO_HIGH
    
    def __init__(self, *args, **kwargs):
        super(RequstCancel, self).__init__(*args, **kwargs)
        self.default_task_show_log_lvl = self.task_show_log_lvl

    def prepare(self):
        self.canceled_requests = []
        reqlst = models.ReqList.objects.filter(processing=True)
        reqlst.update()  # needed to avoid caching and catch external changes
        for req in models.Request.objects.all().order_by('file_name'):
            if not reqlst.filter(pk=req.pk_reqlist):
                # found a new request to process
                self.canceled_requests.append(req)
        if self.canceled_requests:
            self.initial_message = 'found %i canceled requests' % len(self.canceled_requests)
            self.task_force_progress_timeout()

        return self.canceled_requests

    def run_it(self):
        #if not (self.already_parsed.has_key(full_path) and self.already_parsed[full_path] == mtime):
        #    # we dont bother with files we have already checked

        msg = u'Preparing to remove %i requests - waiting for other plugins to stop...' % len(self.canceled_requests)
        self.task_force_progress_timeout()
        self.task_time_to_show(msg)
        sip_task.PLUGINS_MAY_NOT_RUN = True
        
        while threading.active_count() > 2: # main processor and this one...
            self.log('+++ waiting for other processes to terminate...')
            self.log('+++ thread count %i' % threading.active_count())
            time.sleep(5) # waiting for all plugins to abort/complete
            self.task_time_to_show(msg)
            
        cursor = connection.cursor()
        idx = 0
        aborting = False
        icount = len(self.canceled_requests)
        for canceled_req in self.canceled_requests:
            idx += 1
            self.task_force_progress_timeout()
            self.task_time_to_show('Removing %s' % canceled_req.file_name)
            req = models.Request.objects.get(pk=canceled_req.pk)
            req.delete()
            
            # remove obsolete mdrecords
            sql =     ["DELETE FROM base_item_mdrecord"]
            sql.append("WHERE id IN (")
            sql.append("  SELECT md_record FROM base_item_requestmdrecord")
            sql.append("  WHERE request=%i" % canceled_req.pk)
            sql.append(")")
            sql.append(";COMMIT")
            cursor.execute(" ".join(sql))

            # delete bad orphaned uris
            sql =     ["DELETE FROM plug_uris_uri"]
            sql.append("WHERE id IN (")
            sql.append("  SELECT uri FROM plug_uris_requri")
            sql.append("  WHERE req=%i" % canceled_req.pk)
            sql.append(")")
            cursor.execute(" ".join(sql))
            i = cursor.rowcount
            cursor.execute("COMMIT")
            if i:
                self.log('*** removed %i bad uris belonging to deleted request' % i,1)
           
            # remove all orphaned uris
            cursor.execute("delete from plug_uris_uri where mdr_pk = 0; COMMIT")
            
            cursor.execute("DELETE FROM plug_uris_requri WHERE req=%i ;COMMIT" % canceled_req.pk)
            uri_models.ReqUri.objects.update() # make sure cache reflects new content!

            # delete request - mdrecord pointers
            cursor.execute("DELETE FROM base_item_requestmdrecord WHERE request=%i ;COMMIT" % canceled_req.pk)
            
            self.task_time_to_show('Completed')
            
        base_item.MdRecord.objects.update()                    
        uri_models.Uri.objects.update()
        base_item.RequestMdRecord.objects.update()
        rnm = UpdateRequestStats()
        rnm.trigger_timeout()
        rnm.run()

        sip_task.PLUGINS_MAY_NOT_RUN = False
        return True




task_list = [
    Watchdog,
    RequestParseNew,
    NewRequstCreate,
    RequstCancel,
             ]
