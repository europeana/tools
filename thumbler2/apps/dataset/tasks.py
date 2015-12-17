import os.path
import random
import shutil
import tarfile
import time
import urlparse

from django.conf import settings
from django.db import transaction, connection

from xml.sax.saxutils import unescape

from apps.sipmanager import sip_task

from apps.plug_uris import models as uri_models
from apps.statistics.models import Statistics
import models


COMMIT_INTERVALL = 15 # seconds
SNIPPET_PROCESSING_TIME = 1200  # seconds one process will work on tar snippets
                               # process will be restarted, this to avoid
                               # memory shortage on gigantic filesets
TAR_EXTRACT_DIR = os.path.join(settings.TEMP_DIR, 'dataset', 'tarextract')

class DataSet(sip_task.SipTask):
    SHORT_DESCRIPTION = 'Process new datasets'
    # cant be threaded...
    THREAD_MODE = sip_task.SIPT_SINGLE
    PRIORITY = sip_task.SIP_PRIO_INCREASED

    def prepare(self):
        in_progres_datasets = models.DataSet.objects.filter(tar_dir__isnull=False,pid=0)
        if in_progres_datasets:
            self.ds = in_progres_datasets[0]
            return True

        for stat in (models.DS_CREATED, models.DS_RECHECK):
            ds_lst = models.DataSet.objects.filter(status=stat, pid=0)
            if ds_lst:
                self.ds = ds_lst[0]
                return True
        return False


    def run_it(self):
        ds_name = os.path.splitext(os.path.basename(self.ds.ffile.name))[0]
        self.ds = self.grab_item(models.DataSet, self.ds.pk,'parsing imgs from %s' % ds_name)
        if not self.ds:
            return True # all got taken by somebody else

        if self.ds.status == models.DS_RECHECK:
            self.recheck_dataset(ds_name)
        elif self.ds.tar_dir:
            # already in progress, continue to work on it
            self.process_tar_snippets()
        else:
            self.log('Reading dataset %s' % ds_name, 3)
            stats, created = Statistics.objects.get_or_create(ds=self.ds)
            if not created:
                stats.delete()
                stats = Statistics(ds=self.ds)
            stats.set_name = ds_name
            stats.save()
            self.process_dataset()
        self.release_item(models.DataSet, self.ds.pk)
        return True


    def recheck_dataset(self, ds_name):
        sql = ['UPDATE plug_uris_uri SET status=1',
               ",mime_type='',file_type=''",
               ", org_w=0, org_h=0, pid=0, url_hash=''",
               ", content_hash='', err_code=0",
               ", err_msg=''",

               "WHERE id in ( SELECT du.uri_id",
               "FROM dataset_dataseturls du, dataset_dataset ds",
               "WHERE ds.ffile LIKE '%%%s%%'" % ds_name,
               "AND du.ds_id = ds.id)",]
        cursor = connection.cursor()
        self.task_time_to_show('marking all urls as unprocessed for dataset...',force=True)
        cursor.execute(" ".join(sql))
        self.task_time_to_show('manual sql cmd completed...',force=True)
        self.ds.status = models.DS_DONE
        self.ds.save()
        return True


    def process_dataset(self):
        self.ds.status=models.DS_UNZIPPING
        self.ds.save()
        try:
            fname = os.path.join(settings.MEDIA_ROOT, self.ds.ffile.name)
            file_type = os.path.splitext(fname)[-1]
            if file_type == '.tgz':
                self.untar_it(fname)
            #elif file_type == '.zip':
            else:
                # unrecognized file
                self.ds.status = models.DS_UNRECOGNISED_FILE
                raise sip_task.SipTaskException('Dataset file %s could not be processed' % self.ds.ffile.name)

        except sip_task.SipSystemOverLoaded:
            self.log('WARNING: DataSet parsing aborted: %s' % self.ds.ffile.name, 1)
            self.ds.status=models.DS_CREATED # reprocess this one later
        except:
            self.log('ERROR: DataSet processing failed: %s' % self.ds.ffile.name, 1)
            if self.ds.status == models.DS_UNZIPPING:
                # if no specific error has been set
                self.ds.status=models.DS_FAILED
        else:
            self.ds.status=models.DS_DONE
        self.ds.save()


    def untar_it(self, fname_tar):
        self.ds.tar_dir = os.path.join(TAR_EXTRACT_DIR, str(self.pid))
        self.ds.save()
        os.makedirs(self.ds.tar_dir)
        self.task_time_to_show('extracting tar file...',force=True)
        cmd = 'tar xfz %s -C %s' % (fname_tar, self.ds.tar_dir)
        result = self.cmd_execute1(cmd, 3600)
        if result:
            self.clear_tar_dir(self.ds.tar_dir)
            raise sip_task.SipTaskException('Failed to extract tar file %s' % result)
        self.task_time_to_show('Done extracting',force=True)
        self.process_tar_snippets()


    @transaction.commit_manually
    def process_tar_snippets(self):
        baborted = False
        icount = iok = ibad = 0
        t_sync = 0
        t0 = time.time()
        for root, dirs, files in os.walk(self.ds.tar_dir):
            if baborted:
                break
            for ffile in files:
                fname = os.path.join(root, ffile)
                xml = open(fname).read()
                parts = xml.split('<edm:object')[1:]
                for part in parts:
                    raw_uri = part.split('rdf:resource="')[1].split('"/>')[0].strip()
                    uri = unescape(raw_uri)
                    iok += 1
                    try:
                        self.process_uri(uri)
                    except:
                        self.log('*** process_uri() exception!!',1)
                        self.clear_tar_dir(tar_dir)
                        raise
                if not parts:
                    ibad += 1
                os.remove(fname)
                if time.time() > t_sync:
                    transaction.commit()
                    if (time.time() - t0) > SNIPPET_PROCESSING_TIME:
                        baborted = True
                        self.log('aborting processing of %s due to max processing time' % fname,5)
                        break
                    t_sync = time.time() + COMMIT_INTERVALL
                    try:
                        self.task_time_to_show('parsing xml good/bad sofar: %i/%i' % (iok, ibad),
                                               terminate_on_high_load=True)
                    except sip_task.SipSystemOverLoaded:
                         # Terminate in a controled fashion so we can do cleanup
                        baborted = True
                        break


        #print 'OK: %i \tbad %i\t time: %i' % (iok, ibad, time.time() - t0)

        stats = Statistics.objects.get(ds=self.ds)
        stats.record_count = iok
        stats.save()
        if not baborted:
            self.clear_tar_dir()
        transaction.commit()
        return


    def clear_tar_dir(self):
        if (self.ds.tar_dir.find(settings.TEMP_DIR) == 0) and (self.ds.tar_dir.find('..') == -1):
            shutil.rmtree(self.ds.tar_dir)
        self.ds.tar_dir = None
        self.ds.save()
        return




    @transaction.commit_manually
    def untar_it_old(self, fname_tar):
        try:
            tar = tarfile.open(fname_tar)
        except:
            raise sip_task.SipTaskException('Failed to open tar file %s' % self.ds.ffile.name)
        fname = '/tmp/thumblr-%s.xml' % self.pid
        icount = iok = ibad = 0
        t_sync = 0
        t0 = time.time()
        for tarinfo in tar:
            icount += 1
            if not tarinfo.isreg():
                continue # was not regular file
            self.task_time_to_show('%i - links:%i' % (icount,iok))
            xml = tar.extractfile(tarinfo).read()
            try:
                raw_uri = xml.split('<edm:object')[1].split('rdf:resource="')[1].split('"/>')[0].strip()
                uri = unescape(raw_uri)
                #uri = xml.split('<edm:object rdf:resource="')[1].split('"/><')[0]
            except:
                ibad += 1
                continue # no img found
            iok += 1
            try:
                self.process_uri(uri)
            except:
                self.log('*** process_uri() exception!!',1)
                raise
            if time.time() > t_sync:
                transaction.commit()
                t_sync = time.time() + COMMIT_INTERVALL

        print 'OK: %i \tbad %i\t time: %i' % (iok, ibad, time.time() - t0)
        stats = Statistics.objects.get(ds=self.ds)
        stats.record_count = iok
        stats.save()
        transaction.commit()
        tar.close()




    def process_uri(self, url):#,   mdr_id, request_id, itype):
        uri, created = uri_models.Uri.objects.get_or_create(item_type=uri_models.URIT_OBJECT, url=url)
        if (not created) and (uri.status != uri_models.URIS_COMPLETED):
            # flag url for reprocessing
            uri.status = uri_models.URIS_CREATED
        uri.uri_source = self.get_uri_source(url).pk
        uri.save()

        models.DataSetUrls.objects.create(ds=self.ds, uri=uri)
        return True





    def get_uri_source(self, url):
        srvr_name = urlparse.urlsplit(url).netloc.lower()
        try:
            # See if the source is already existing
            uri_source = uri_models.UriSource.objects.get(name_or_ip=srvr_name)
        except:
            # not found create it
            uri_source = uri_models.UriSource(name_or_ip=srvr_name)
            self.log(u'Created new urisource: %s' % uri_source.name_or_ip, 5)
            uri_source.save()
        return uri_source



# List of active plugins from this file
task_list = [
    DataSet,
]
