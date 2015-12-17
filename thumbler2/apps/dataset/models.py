
import os

from django.db import models
#from django.db.models.signals import post_delete

from utils.gen_utils import dict_2_django_choice

from apps.plug_uris.models import Uri

# Create your models here.



URI_ABS_MAX_LENGTH = 1023
MIME_TYPE_FIELD_LENGTH = 50
DATASET_FNAME_PREFIX = 'datasets'

DS_CREATED = 0  # file uploaded by admin interface
DS_RECHECK = 5
DS_UNZIPPING = 10 # extracting uris
DS_DONE = 20 # all uris extracted
DS_UNRECOGNISED_FILE = 400
DS_FAILED = 9999

DS_STATES = {
    DS_CREATED: 'created',
    DS_RECHECK: 'recheck',
    DS_UNZIPPING: 'unzipping',
    DS_DONE: 'done',
    DS_FAILED: 'failed',
    DS_UNRECOGNISED_FILE: 'unrecognized file'
}

DS_NO_ERROR = 0
DS_OTHER_ERROR = 1

DS_ERR_CODES = {
    DS_NO_ERROR: '',
    DS_OTHER_ERROR: 'other error',
}


class DataSet(models.Model):
    ffile = models.FileField(upload_to=DATASET_FNAME_PREFIX)
    #furl = models.URLField()
    status = models.IntegerField(choices=dict_2_django_choice(DS_STATES),
                                 default=DS_CREATED, db_index=True)
    pid = models.FloatField(default=0, db_index=True) # what process 'owns' this item
    err_code = models.IntegerField(choices=dict_2_django_choice(DS_ERR_CODES),
                                   default = DS_NO_ERROR, db_index=True)
    err_msg = models.TextField(default='')
    time_created = models.DateTimeField(auto_now_add=True)
    tar_dir = models.CharField(null=True, max_length=250)
    def __unicode__(self):
        return self.ffile.name

def delete_ds_file(sender, **kwargs):
    ds = kwargs.get('instance')
    try:
        os.remove(ds.ffile.path)
    except:
        pass

models.signals.post_delete.connect(delete_ds_file, DataSet)




class DataSetUrls(models.Model):
    ds = models.ForeignKey(DataSet)
    uri = models.ForeignKey(Uri)#, related_name='+')

def clear_pids():
    qs = DataSet.objects.exclude(pid=0)
    for ds in qs:
        ds.pid=0
        if ds.err_code == DS_NO_ERROR:
            ds.status = DS_CREATED
        ds.save()




