from django.db import models

from apps.dataset.models import DataSet

# Create your models here.

class Statistics(models.Model):
    ds = models.ForeignKey(DataSet, unique=True, related_name='+')
    set_name = models.CharField(max_length=300,
                            help_text='relative filename, dont store path, system will find it with os.walk() and timestamp...')
    pid = models.FloatField(default=0, db_index=True) # what process 'owns' this item
    record_count = models.IntegerField(default=0)

    count_1 = models.IntegerField(default=0)
    ok_1 = models.IntegerField(default=0)
    bad_1 = models.IntegerField(default=0)
    waiting_1 = models.IntegerField(default=0)
    ratio_1 = models.DecimalField(max_digits=5, decimal_places=2, default=0.00)

    #count_2 = models.IntegerField(default=0)
    #ok_2 = models.IntegerField(default=0)
    #bad_2 = models.IntegerField(default=0)
    #waiting_2 = models.IntegerField(default=0)
    #ratio_2 = models.CharField(max_length=10,default='0.00')

    #count_3 = models.IntegerField(default=0)
    #ok_3 = models.IntegerField(default=0)
    #bad_3 = models.IntegerField(default=0)
    #waiting_3 = models.IntegerField(default=0)
    #ratio_3 = models.CharField(max_length=10,default='0.00')

    obsolete = models.BooleanField(default=False)
    
    class Meta:
        ordering = ['set_name']
        

def clear_pids():
    stats = Statistics.objects.exclude(pid=0)
    for item in stats:
        item.pid=0
        item.save()

