from django.db import models

from apps.dataset.models import DataSet

# Create your models here.

class WebserverStats(models.Model):
    name_or_ip = models.CharField(max_length=200)
    uri_source = models.IntegerField()
    remaining = models.IntegerField(default=0)
    obsolete = models.BooleanField(default=False)
    time_lastcheck = models.DateTimeField(auto_now_add=True)
    eta = models.DateTimeField(auto_now_add=True)
    
    def __unicode__(self):
        return self.name_or_ip
    
    
class Statistics(models.Model):
    ds = models.ForeignKey(DataSet, unique=True, related_name='+')
    set_name = models.CharField(max_length=300, default='',
                            help_text='relative filename, dont store path, system will find it with os.walk() and timestamp...')
    pid = models.FloatField(default=0, db_index=True) # what process 'owns' this item
    obsolete = models.BooleanField(default=False) # if this record has been updated this UpdateRequestStats run
    record_count = models.IntegerField(default=0)
    

    count_1 = models.IntegerField(default=0)
    ok_1 = models.IntegerField(default=0)
    bad_1 = models.IntegerField(default=0)
    waiting_1 = models.IntegerField(default=0)
    ratio_1 = models.DecimalField(max_digits=5, decimal_places=2, default=0.00)

    
    class Meta:
        ordering = ['set_name']
        

def clear_pids():
    stats = Statistics.objects.exclude(pid=0)
    for item in stats:
        item.pid=0
        item.save()

