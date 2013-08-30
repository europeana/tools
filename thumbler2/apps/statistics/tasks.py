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


 
SELECT r.id,r.file_name,r.record_count, COUNT(ru.id) url_c
FROM plug_uris_requri ru, dummy_ingester_request r 
WHERE ru.req=r.id
GROUP BY r.id,r.file_name,r.record_count

req_id, file_name, record_count, url_count


SELECT r.file_name, COUNT(ru.id)
FROM plug_uris_requri ru, dummy_ingester_request r 
WHERE ru.req=r.id AND ru.status=100 AND err_code=0 
GROUP BY r.file_name

file_name, url_ok_count

SELECT r.file_name, COUNT(ru.id)
FROM plug_uris_requri ru, dummy_ingester_request r 
WHERE ru.req=r.id AND  err_code>0 
GROUP BY r.file_name
file_name, url_bad_count

 
        itm['waiting'] = link_count - is_ok - is_bad
        itm['ratio'] = s_calc_ratio_bad(itm['ok'], itm['bad'])

"""


import os.path
import time

from django.db import connection
from django.conf import settings

from apps.dataset.models import DataSet, DataSetUrls
from apps.sipmanager import sip_task
import apps.plug_uris.models as uri_models
import models


from utils.timestamp_file import TimeStampLinkStats

LAST_RUN = time.time() - settings.STATS_UPDATE_INTERVALL + 180

class UpdateRequestStats(sip_task.SipTask):
    SHORT_DESCRIPTION = 'updating statistics'
    PRIORITY = sip_task.SIP_PRIO_LOW
    THREAD_MODE = sip_task.SIPT_SINGLE
    
    def prepare(self, now=False):
        b = False
        if now:
            b = True
        elif settings.STATS_UPDATE_INTERVALL and (time.time() > LAST_RUN + settings.STATS_UPDATE_INTERVALL):
            b = True
        return b
                
    def run_it(self, now=False):
        global LAST_RUN
        models.Statistics.objects.all().delete()
        b_abort = False
        for ds in models.DataSet.objects.all():
            stat = models.Statistics(ds=ds, set_name=os.path.basename(str(ds.ffile)))
            self.log('Generating stats for %s' % stat.set_name, 9)
            try:
                self.process_one_dataset(stat)
            except sip_task.SipSystemOverLoaded:
                b_abort = True
            stat.save()
            self.log('Completed stats for %s' % stat.set_name, 9)
            if b_abort:
                break
            
        TimeStampLinkStats().set()
        LAST_RUN = time.time()


    def process_one_dataset(self, stat):
        stat.ok_1 = stat.bad_1 = stat.waiting_1 = 0
        
        cursor = connection.cursor()
        l = ["select u.status,count(u.status)"]
        l.append(",u.err_code, count(u.err_code)")
        l.append("from dataset_dataseturls du, plug_uris_uri u")
        l.append("where u.id=du.uri_id")
        l.append("and du.ds_id=%i" % stat.ds_id)
        l.append("group by du.ds_id ,u.status ,u.err_code")
        l.append("order by du.ds_id, u.status")
        cursor.execute(" ".join(l))
        for status, status_count, err_code, err_count in cursor.fetchall():
            if status == uri_models.URIS_COMPLETED:
                stat.ok_1 += status_count
            elif err_code:
                stat.bad_1 +=  err_count
            elif status in (uri_models.URIS_CHECK_BACKLOG, uri_models.URIS_CREATED):
                stat.waiting_1 += status_count
            else:
                self.log('*** unhandled statistics output: %i,%i,%i,%i' % (status, status_count, err_code, err_count),1)
        stat.ratio_1 = s_calc_ratio_bad(stat.ok_1, stat.bad_1)
        stat.save()
        return True
            


def s_calc_ratio_bad(good, bad):
    s = u'%0.2f' % (100 - calc_ratio(bad, good+ bad))
    return s

def calc_ratio(part, whole):
    if not whole:
        # avoid divide by zero
        return 100
    f =  (part/float(whole)) * 100
    return f



task_list = [
    UpdateRequestStats,
]
