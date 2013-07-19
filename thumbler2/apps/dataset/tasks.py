import os.path
import tarfile
import time
import urlparse

from django.conf import settings
from django.db import transaction

from apps.sipmanager import sip_task

from apps.plug_uris import models as uri_models
from apps.statistics.models import Statistics
import models


COMMIT_INTERVALL = 15 # seconds



class DataSet(sip_task.SipTask):
    SHORT_DESCRIPTION = 'Processnew datasets'
    # cant be threaded...
    THREAD_MODE = sip_task.SIPT_THREADABLE
    PRIORITY = sip_task.SIP_PRIO_INCREASED
   
    def prepare(self):
        ds_lst = self.get_unprocessed_datasets()
        if ds_lst:
            self.ds = ds_lst[0]
            return True
        return False
        
    def run_it(self):
        ds_name = os.path.splitext(os.path.basename(self.ds.ffile.name))[0]
        self.ds = self.grab_item(models.DataSet, self.ds.pk,'parsing imgs from %s' % ds_name)
        if not self.ds:
            return True # all got taken by somebody else

        stats, created = Statistics.objects.get_or_create(ds=self.ds, set_name=ds_name)
        if not created:
            stats.delete()
            stats = Statistics(ds=self.ds, set_name=ds_name)
        stats.save()        
        self.process_dataset()
        self.release_item(models.DataSet, self.ds.pk)
        return True

    def process_dataset(self):
        self.ds.status=models.DS_UNZIPPING
        self.ds.save()
        try:
            self.untar_it()
        except sip_task.SipSystemOverLoaded:
            self.ds.status=models.DS_CREATED # reprocess this one later
        else:            
            self.ds.status=models.DS_DONE
        self.ds.save()
        

    @transaction.commit_manually
    def untar_it(self):
        t0 = time.time()
        fname = os.path.join(settings.MEDIA_ROOT, self.ds.ffile.name) 
        try:
            tar = tarfile.open(fname)
        except:
            raise sip_task.SipTaskException('Failed to open tar file %s' % self.ds.ffile.name)
        fname = '/tmp/thumblr-%s.xml' % self.pid
        icount = iok = ibad = 0
        t_sync = 0
        for tarinfo in tar:
            icount += 1
            if not tarinfo.isreg():
                continue # was not regular file
            self.task_time_to_show('%i - links:%i' % (icount,iok))
            xml = tar.extractfile(tarinfo).read()
            try:
                uri = xml.split('<edm:object rdf:resource="')[1].split('"/><')[0]
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
        if created:
            uri_source = self.get_uri_source(url)
            uri.uri_source = uri_source.pk
            uri.save()

        models.DataSetUrls.objects.create(ds=self.ds, uri=uri)
        return True
        
        
        
        
        
    def get_uri_source(self, url):
        srvr_name = urlparse.urlsplit(url).netloc.lower()
        try:
            # See if the source is already existing
            uri_sources = uri_models.UriSource.objects.filter(name_or_ip=srvr_name)
            if uri_sources:
                uri_source = uri_sources[0]
                b = True
            else:
                b = False
        except:
            b = False
        if not b:
            # not found create it
            uri_source = uri_models.UriSource(name_or_ip=srvr_name)
            self.log(u'Created new urisource: %s' % srvr_name, 5)
            uri_source.save()
        return uri_source
        
        
    def get_unprocessed_datasets(self):
        return models.DataSet.objects.filter(status=models.DS_CREATED,pid=0)
        
    


# List of active plugins from this file
task_list = [
    DataSet,
]
