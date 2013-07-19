import time

def exporter(sip_task, collection, django_mod, item_mapper):
    label = 'Exporting %s to mongodb' % django_mod.__name__
    rec_count = django_mod.objects.count()
    sip_task.task_starting(label, rec_count)
    rec_idx = 0
    t1 = time.time()
    for old_item in django_mod.objects.all():
        rec_idx += 1
        new_item = item_mapper(old_item)
        iid = collection.insert(new_item)
        sip_task.task_time_to_show(rec_idx)
    sip_task.log('\t%i items Done! (%0.1f seconds)' % (rec_idx, time.time() - t1))

