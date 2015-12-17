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


import os
import time

from django.db import connection
from django.conf import settings
from django.db.models import Count, Min, Sum, Max, Avg

from apps.dataset.models import DataSet, DataSetUrls, DATASET_FNAME_PREFIX
from apps.sipmanager import sip_task
import apps.plug_uris.models as uri_models
import models


from django.utils import timezone
import datetime


from utils.timestamp_file import TimeStampLinkStats

LAST_RUN = time.time() - settings.STATS_UPDATE_INTERVALL + 180
if 'WINGDB_ACTIVE' in os.environ.keys():
    LAST_RUN = 0


class UpdateRequestStats(sip_task.SipTask):
    SHORT_DESCRIPTION = 'updating statistics'
    PRIORITY = sip_task.SIP_PRIO_NORMAL
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
        
        models.Statistics.objects.update(obsolete=True)

        ds_count = models.DataSet.objects.count()
        self.task_starting('Updating statistics', ds_count, display=False)
        idx = 0
        for ds in models.DataSet.objects.all():
            idx += 1
            stat, created = models.Statistics.objects.get_or_create(ds=ds)
            if created or (not stat.set_name):
                stat.set_name = os.path.split(stat.ds.ffile.name)[1]
                stat.save()
            self.task_set_label(stat.set_name)
            try:
                self.task_time_to_show(idx, terminate_on_high_load=True)
            except sip_task.SipSystemOverLoaded:
                break # this can safely be done later

            self.log('Generating stats for %s' % stat.set_name, 9)
            try:
                self.process_one_dataset(stat)
            except:
                pass
            self.log('Completed stats for %s' % stat.set_name, 10)

        # delete all items that was not updated, ie tat has been removed from dataset
        models.Statistics.objects.filter(obsolete=True).delete()
        self.process_webservers()
        TimeStampLinkStats().set()
        
        LAST_RUN = time.time()

    def process_webservers(self):
        cursor = connection.cursor()
        sql = ['select uri_source, s.name_or_ip, count(uri_source) as antal']
        sql.append('from plug_uris_uri u, plug_uris_urisource s')
        sql.append('where status=1 and err_code=0 and s.id = u.uri_source')
        sql.append('group by uri_source, s.name_or_ip order by antal desc')
        self.log('UpdateRequestStats: gatering stats per webserver', 8)
        cursor.execute(" ".join(sql))
        webservers = {}
        for web_serv_id, web_serv_name, count in cursor.fetchall():
            webservers[web_serv_name] = {'uri_source':web_serv_id, 'item_count':count}
        

        self.log('UpdateRequestStats: updating WebserverStats table', 8)
        models.WebserverStats.objects.all().update(obsolete=True)
        
        for name in webservers.keys():
            ws, created = models.WebserverStats.objects.get_or_create(name_or_ip=name,uri_source=webservers[name]['uri_source'])
            processed_recs = ws.remaining - webservers[name]['item_count']
            if processed_recs > 0:
                epoc = (timezone.now() - ws.time_lastcheck).seconds
                burndown = float(processed_recs) / epoc
                eta_s = ws.remaining / burndown
                eta = timezone.now() + datetime.timedelta(seconds=eta_s)
            else:
                eta = timezone.now()
            ws.eta = eta
            ws.remaining = webservers[name]['item_count']
            ws.obsolete=False
            ws.save()
            
        # remove db items for no longer present items
        models.WebserverStats.objects.filter(obsolete=True).delete()
        self.log('UpdateRequestStats: WebserverStats done', 8)
        

    def process_one_dataset(self, stat):
        cursor = connection.cursor()
        stat.ok_1 = stat.bad_1 = stat.waiting_1 = 0

        sql = ['select count (*) from plug_uris_uri where id in (select uri_id from dataset_dataseturls']
        sql.append('where ds_id=%i)' % stat.ds_id)        
        cursor.execute(" ".join(sql))
        stat.count_1 = cursor.fetchone()[0]

        stat.ok_1 = self.record_count(stat.ds_id, True)
        stat.bad_1 = self.record_count(stat.ds_id, False)
        stat.waiting_1 = stat.count_1 - stat.ok_1 - stat.bad_1
        stat.ratio_1 = s_calc_ratio_bad(stat.ok_1, stat.bad_1)
        stat.obsolete = False
        stat.save()
        return True



    def record_count(self, ds_id, is_ok):
        cursor = connection.cursor()
        if is_ok:
            status = 'status=100'
        else:
            status = 'status!=100 and status > 1'

        l = ['select count(*) from plug_uris_uri where']
        l.append(status)
        l.append('and id in ( select uri_id from  dataset_dataseturls where ds_id=%i)' % ds_id)
        t00 = time.time()
        cursor.execute(" ".join(l))
        return cursor.fetchone()[0]



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