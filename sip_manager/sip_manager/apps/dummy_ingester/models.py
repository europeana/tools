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

import datetime
import os

from django.db import models, connection
from django.conf import settings

from utils.gen_utils import dict_2_django_choice, count_records



AUTOGEN_NAME = 'Grabbed from svn'


PATH_COLLECTIONS = settings.PATH_COLLECTIONS



    
    
# REQS_ = Request status
REQS_PRE = 0 # unoficial state, only in dummy_ingestion
REQS_INIT = 1
REQS_IMPORTED = 2
REQS_ABORTED = 3
REQS_SIP_PROCESSING = 4
REQS_PENDING_VALIDATION_SIGNOFF = 5
REQS_PENDING_AIP_SIGNOFF = 6
REQS_CREATING_AIP = 7
REQS_AIP_COMPLETED = 8

REQS_STATES = {
    REQS_PRE: 'unprocessed',
    REQS_INIT: 'under construction',
    REQS_IMPORTED: 'import completed',
    REQS_ABORTED: 'aborted',
    REQS_SIP_PROCESSING: 'sip processing',
    REQS_PENDING_VALIDATION_SIGNOFF: 'pending validation sign off',
    REQS_PENDING_AIP_SIGNOFF: 'pending AIP sign off',
    REQS_CREATING_AIP: 'creating AIP',
    REQS_AIP_COMPLETED: 'AIP completed',

    }


PRTH_WAITING = 0
PRTH_PROCESSING = 1
PRTH_FAILED = 2

PRTH_STATES = {
    PRTH_WAITING : 'waiting',
    PRTH_PROCESSING : 'being processed',
    PRTH_FAILED : 'failed',
    }


class PrioThumbs(models.Model):
    status = models.IntegerField(choices=dict_2_django_choice(PRTH_STATES),
                                 default=PRTH_WAITING, db_index=True)
    url = models.CharField(max_length=1023,db_index=True)
    err_msg = models.CharField(max_length=200, blank=True)
    

class Request(models.Model):
    pk_reqlist = models.IntegerField()
    status = models.IntegerField(choices=dict_2_django_choice(REQS_STATES),
                                 default = REQS_PRE)
    record_count = models.IntegerField() # no of records in request
    # we dont store path, find it by os.walk and check time_stamp
    file_name = models.CharField(max_length=300,
                                 help_text='relative filename, dont store path, system will find it with os.walk() and timestamp...')
    rel_path = models.CharField(max_length=550)
    time_created = models.DateTimeField(editable=False) # is mtime for file being processed
    pid = models.FloatField(default=0) # what process 'owns' this item
    err_msg = models.CharField(max_length=200, blank=True)

    def __unicode__(self):
        return self.file_name


    
    
        
  
class ReqList(models.Model):
    name = models.CharField(max_length=200, unique=True)
    rel_path = models.CharField(max_length=450)
    file_date = models.DateTimeField(editable=False)
    rec_count = models.IntegerField(default=0)
    last_processed = models.DateTimeField(null=True,editable=False)
    concurrency = models.IntegerField(default=1)
    processing = models.BooleanField(default=False)
    
    
    def __unicode__(self):
        return self.name
    
    def save(self, *args, **kwargs):
        full_path = os.path.join(PATH_COLLECTIONS, self.rel_path)
        self.file_date = datetime.datetime.fromtimestamp(os.path.getmtime(full_path))
        super(ReqList, self).save(*args, **kwargs)
