import os

from django.db import models
from django.conf import settings


from utils.gen_utils import dict_2_django_choice
from apps.dummy_ingester.models import Request

# Create your models here.


class IngQueue(models.Model):
    """
    Lists all uris by request
    """
    req = models.CharField(max_length=50) # mostly relevant for objects...

    
    
    class Meta:
        db_table = 'ingqueue_ingqueue'
        app_label = 'Ingestion Queue'
        verbose_name = 'Ingestion Queue'
        verbose_name_plural = 'Queues'
    

    def __unicode__(self):
        return self.req
    
    
    
    
SUBM_PROC = 1
SUBM_ACCEPT = 2 #  the uri responds and returns an OK
SUBM_PRODUCTION = 3
    
SUBM_STATES = {
    SUBM_PROC: 'processing',
    SUBM_ACCEPT: 'on acceptance server',
    SUBM_PRODUCTION: 'in production',
    }

class Submitter(models.Model):
    request = models.ForeignKey(Request, unique=True)
    state  = models.IntegerField(choices=dict_2_django_choice(SUBM_STATES),
                                 default = SUBM_PROC,db_index=True)
    
    class Meta:
        db_table = 'ingqueue_submitter'
        app_label = 'Submittion status'
        verbose_name = 'Request'
        verbose_name_plural = 'Requests'
    


    def __unicode__(self):
        return self.request.__str__()
