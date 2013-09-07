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

import codecs
import os.path
import time

from django.conf import settings

from utils import files_to_sync
from utils.execute_cmd import CMD_RETRY_RETCODE, CMD_RETRY_STDERR, CMD_RETRY_LIMIT

from apps.sipmanager import sip_task
try:
    from apps.log import models as log
    db_logger = True
except:
    db_logger = False


SIP_OBJ_FILES = settings.SIP_OBJ_FILES
NODE_NAME = settings.NODE_NAME

SYNC_DEST = settings.SYNC_DEST

if SYNC_DEST[-1] == '/':
    SYNC_DEST = SYNC_DEST[:-1]
    


class SyncLeftovers(sip_task.SipTask):
        SHORT_DESCRIPTION = 'Moves partial adding files to sync-wait on start up'
        INIT_PLUGIN = True
        PLUGIN_TAXES_DISK_IO = True

        def run_it(self):
            fts = files_to_sync.FilesToSync()
            abandoned_files = fts.list_files_in_state(files_to_sync.DIR_ST_ADDING)
            if not abandoned_files:
                return True
            self.log('Found leftover adding files, moving them to sync-wait', 2)
            for fname in abandoned_files:
                self.log('Moving file adding->sync wait : %s' % fname, 7) 
                fts.set_state(files_to_sync.DIR_ST_SYNC_WAIT, fname)
            return True
            

class SyncManager(sip_task.SipTask):
    SHORT_DESCRIPTION = 'Ensures images are moved on'
    PLUGIN_TAXES_NET_IO = True
    THREAD_MODE = sip_task.SIPT_SINGLE
    PRIORITY = sip_task.SIP_PRIO_HIGH
    
    def __init__(self, debug_lvl=-9, run_once=False, touch_file=None):
        sip_task.SipTask.__init__(self, debug_lvl, run_once)
        self.touch_file = touch_file
       
    def prepare(self):
        self.fts = files_to_sync.FilesToSync()
        self.syncable_files = self.fts.list_files_in_state(files_to_sync.DIR_ST_SYNC_WAIT)
        return self.syncable_files

    def run_it(self):
        self.count_items = 0
        self.sync_dests = SYNC_DEST
        if not isinstance(self.sync_dests, (list,tuple)):
            self.sync_dests = [self.sync_dests]
        
        #
        # calculate grand total of files to sync
        #
        sync_list = []
        for fname in self.syncable_files:
            retcode, stdout, stderr = self.cmd_execute_output('wc -l %s' % fname)
            try:
                lines = int(stdout.split()[0])
            except:
                raise sip_task.SipTaskException('Failed to calculate linecount for file: %s' % fname)
            self.count_items += lines
            sync_list.append((fname, lines))

            
        self.task_starting('Syncing %i tasks (%i files)' % (len(sync_list), self.count_items), self.count_items)
        
        self.task_force_progress_timeout()
        self.task_time_to_show(0, terminate_on_high_load=True)
        t_timeout = time.time() + 3600 * 1 # hours max processing time
        self.idx_offset = 0 # keep count of previous files
        for fname, line_count in sync_list:
            if not os.path.exists(fname):
                if db_logger:
                    el = log.ErrLog(err_code=log.LOGE_IMG_SYNC_ERR,
                                    msg='File missing',
                                    item_id=fname,
                                    plugin_module=NODE_NAME,
                                    plugin_name=self.__class__.__name__)
                    el.save()
                return True
            
            if (time.mktime(time.localtime()) - os.path.getmtime(fname)) < 30:
                self.log('SyncManager not moving file cause its to recently changed %s' % fname, 7)
                continue # dont touch completely fresh files, ensure copying is completed
            
            msg = 'Sending content of %s' % fname
            self.log(msg, 7)
            
            #b_stopping = self.process_by_line(fname, line_count, self.sync_dests)
            b_stopping = self.process_by_file(fname, line_count)

            
            if b_stopping:
                self.log('+++ SyncManager was asked to terminate when processing [%s]' % fname, 1)
                break
            
            # by doing this after the is stopping check, in case of error
            # we keep the sync file, and it will be reprocessed on next run
            self.move_sync_file(fname)
            
            if self.touch_file:
                # when run from syncer.py we want to touche this after each file is synced
                os.utime(self.touch_file, None)
            

            try:
                self.task_time_to_show(line_count + self.idx_offset, terminate_on_high_load=True)
            except sip_task.SipSystemOverLoaded:
                 # Terminate in a controled fashion so we can do cleanup
                break

            if time.time() > t_timeout:
                self.log('+++ SyncManager terminated due to max processing time', 1)
                break
            
            self.idx_offset += line_count
            
        return True



    def move_sync_file(self, fname):
        #
        # send this file to the SYNC_WAIT container in recieving end, for further syncing
        #
        for sync_dest in self.sync_dests:
            cmd = 'rsync %s %s/%s/%s/' % (fname, sync_dest, 
                                            files_to_sync.FILE_LISTS_DIR_NAME,
                                            files_to_sync.DIR_ST_SYNC_WAIT)
            retcode, stdout, stderr = self.cmd_execute_output(cmd,
                                                              retry_cond = {CMD_RETRY_RETCODE: 255,
                                                                            CMD_RETRY_STDERR: 'Connection timed out',
                                                                            })
            if retcode or stderr:
                self.log('======== stdout =======\n%s' % stdout, 1)
                self.log('======== stderr =======\n%s' % stderr, 1)
                self.log('======== retcode =======\n%s' % retcode, 1)
                raise sip_task.SipTaskException('SyncManager failed to send syncfile [%s] to [%s] - msg: %s' % (fname, sync_dest, stderr))
        self.fts.set_state(files_to_sync.DIR_ST_DELIVERED, fname)


    def process_by_file(self, file_list, line_count):
        b_stopping = False
        self.set_label('%s (%i)' % (file_list, line_count), prefix='r:')
        try:
            self.task_time_to_show(self.idx_offset + 1, terminate_on_high_load=True)
        except sip_task.SipSystemOverLoaded:
             # Terminate in a controled fashion so we can do cleanup
            return True
        for sync_dest in self.sync_dests:
            if sip_task.PLUGINS_MAY_NOT_RUN or sip_task.IS_TERMINATING:
                # shutting down, ok be killed of just die quietly
                b_stopping = True
                break
            cmd = 'rsync -avP --exclude=original/  --files-from=%s %s %s' % (file_list, SIP_OBJ_FILES, sync_dest)
            try:
                retcode, stdout, stderr = self.cmd_execute_output(cmd,
                                                                  retry_cond = {CMD_RETRY_RETCODE: 255,
                                                                                #CMD_RETRY_STDERR: 'Connection timed out',
                                                                                })
                if retcode or stderr:
                    self.log('======== stdout =======\n%s' % stdout, 1)
                    self.log('======== stderr =======\n%s' % stderr, 1)
                    self.log('======== retcode =======\n%s' % retcode, 1)
                    msg = u'%s [%s] out:%s err:%s' % (file_list, retcode, stdout, stderr)
                    if db_logger:
                        el = log.ErrLog(err_code=log.LOGE_IMG_SYNC_ERR,
                                        msg=msg,
                                        item_id=file_list,
                                        plugin_module=NODE_NAME,
                                        plugin_name=self.__class__.__name__)
                        el.save()
                    raise sip_task.SipTaskException(msg)
            except:
                if sip_task.PLUGINS_MAY_NOT_RUN or sip_task.IS_TERMINATING:
                    # shutting down, ok be killed just die quietly
                    b_stopping = True
                else:
                    self.log('SyncManager failed to send content of %s\n%s' % (file_list, stderr), 1)
                    self.log('== rsync problem, shutting down', 1)
                    sip_task.PLUGINS_MAY_NOT_RUN = True
                    sip_task.IS_TERMINATING = True
                    return True
            if b_stopping:
                break
        return b_stopping




    def process_by_line(self, fname, line_count, sync_dests):
        self.set_label('%s (%i)' % (fname, line_count), prefix='s:')
        b_stopping = False
        idx = 0
        for line in codecs.open(fname, 'r', 'utf-8').readlines():
            idx += 1
            if idx > line_count:
                b_stopping = True
                break
            try:
                self.sync_one_file(line.strip(), sync_dests)
            except:
                if sip_task.PLUGINS_MAY_NOT_RUN or sip_task.IS_TERMINATING:
                    # shutting down, ok be killed just die quietly
                    b_stopping = True
                    return b_stopping
                self.log('*** Failed to process file: %s' % fname,1)
                raise
                
            try:
                self.task_time_to_show(idx + self.idx_offset, terminate_on_high_load=True)
            except sip_task.SipSystemOverLoaded:
                 # Terminate in a controled fashion so we can do cleanup
                b_stopping = True
                break

        return b_stopping
    
    
    def sync_one_file(self, rel_fname, sync_dests):
        if not isinstance(sync_dests, (list,tuple)):
            sync_dests = [sync_dests]
        src = os.path.join(SIP_OBJ_FILES, rel_fname)

        if not os.path.exists(src):
            if db_logger:
                el = log.ErrLog(err_code=log.LOGE_IMG_SYNC_ERR,
                                msg='File missing',
                                item_id=rel_fname,
                                plugin_module=NODE_NAME,
                                plugin_name=self.__class__.__name__)
                el.save()
            return
        for sync_dest in sync_dests:
            dst = '%s/%s' % (sync_dest, rel_fname) # syncdest might include usernames and such cant use os,path.join
            cmd = 'scp -pr %s %s' % (src, dst)
            try:
                retcode, stdout, stderr = self.cmd_execute_output(cmd)
                if retcode or stdout or stderr:
                    msg = u'%s [%s] out:%s err:%s' % (rel_fname, retcode, stdout, stderr)
                    if db_logger:
                        el = log.ErrLog(err_code=log.LOGE_IMG_SYNC_ERR,
                                        msg=msg,
                                        item_id=rel_fname,
                                        plugin_module=NODE_NAME,
                                        plugin_name=self.__class__.__name__)
                        el.save()
                    raise sip_task.SipTaskException(msg)
            except:
                msg = 'SyncManager failed to send %s to %s' % (src, dst)
                self.log(msg, 1)
                raise sip_task.SipTaskException(msg)
        return
        
    
    def set_label(self, fname, prefix=''):
        foo, rel_currentfilelist = os.path.split(fname)
        self.task_set_label('%s%s' % (prefix, rel_currentfilelist))
        self.task_force_progress_timeout() # next update will be about a new file...
        
    

"""
 Find items using the bad hash...
 
select * from plug_uris_uri where url_hash = '121F1D87730869A8E857C054F8CBE045AD4111ADB03BD7221E346EE6A8534A48'


pick one of above ids and check for request

select * from plug_uris_requri where uri = 10435556



alter table plug_uris_uri rename to old_uris_uri;

-- Table: plug_uris_uri

CREATE TABLE plug_uris_uri
(
  id serial NOT NULL,
  mdr_pk integer NOT NULL,
  status integer NOT NULL,
  item_type integer NOT NULL,
  mime_type character varying(50) NOT NULL,
  file_type character varying(150) NOT NULL,
  org_w integer NOT NULL,
  org_h integer NOT NULL,
  uri_source integer NOT NULL,
  pid double precision NOT NULL,
  url character varying(1024) NOT NULL,
  url_hash character varying(64) NOT NULL,
  content_hash character varying(64) NOT NULL,
  err_code integer NOT NULL,
  err_msg text NOT NULL,
  time_created timestamp with time zone NOT NULL,
  time_lastcheck timestamp with time zone NOT NULL,
  req integer NOT NULL DEFAULT 0,
  CONSTRAINT plug_uris_uri_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

CREATE UNIQUE INDEX plug_uris_uri_iturl
  ON plug_uris_uri
  USING btree
  (item_type, url);


  
  -- Index: plug_uris_uri_err_code
  
  -- DROP INDEX plug_uris_uri_err_code;
  
  CREATE INDEX plug_uris_uri_err_code
    ON plug_uris_uri
    USING btree
    (err_code);
  
  -- Index: plug_uris_uri_item_type
  
  -- DROP INDEX plug_uris_uri_item_type;
  
  CREATE INDEX plug_uris_uri_item_type
    ON plug_uris_uri
    USING btree
    (item_type);
  
  -- Index: plug_uris_uri_mdr_pk
  
  -- DROP INDEX plug_uris_uri_mdr_pk;
  
  CREATE INDEX plug_uris_uri_mdr_pk
    ON plug_uris_uri
    USING btree
    (mdr_pk);
  
  -- Index: plug_uris_uri_pid
  
  -- DROP INDEX plug_uris_uri_pid;
  
  CREATE INDEX plug_uris_uri_pid
    ON plug_uris_uri
    USING btree
    (pid);
  
  -- Index: plug_uris_uri_req
  
  -- DROP INDEX plug_uris_uri_req;
  
  CREATE INDEX plug_uris_uri_req
    ON plug_uris_uri
    USING btree
    (req);
  
  -- Index: plug_uris_uri_status
  
  -- DROP INDEX plug_uris_uri_status;
  
  CREATE INDEX plug_uris_uri_status
    ON plug_uris_uri
    USING btree
    (status);
  
  -- Index: plug_uris_uri_uri_source
  
  -- DROP INDEX plug_uris_uri_uri_source;
  
  CREATE INDEX plug_uris_uri_uri_source
    ON plug_uris_uri
    USING btree
    (uri_source);
  
  -- Index: plug_uris_uri_url
  
  -- DROP INDEX plug_uris_uri_url;
  
  CREATE INDEX plug_uris_uri_url
    ON plug_uris_uri
    USING btree
    (url);
  
  -- Index: plug_uris_uri_url_like
  
  -- DROP INDEX plug_uris_uri_url_like;
  
  CREATE INDEX plug_uris_uri_url_like
    ON plug_uris_uri
    USING btree
    (url varchar_pattern_ops);
  


"""

task_list = [
    SyncLeftovers, 
    SyncManager,
    ]
