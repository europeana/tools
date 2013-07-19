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


import time
from django.db import connection
from django.conf import settings

from apps.sipmanager import sip_task
import models


from utils.timestamp_file import TimeStampLinkStats

LAST_RUN = time.time() - settings.STATS_UPDATE_INTERVALL + 180

class UpdateRequestStats(sip_task.SipTask):
    SHORT_DESCRIPTION = 'updating statistics'
    PRIORITY = sip_task.SIP_PRIO_LOW
    THREAD_MODE = sip_task.SIPT_NOT
    
    def prepare(self, now=False):
        if now:
            return True
        elif not settings.STATS_UPDATE_INTERVALL:
            return False
        elif time.time() > LAST_RUN + settings.STATS_UPDATE_INTERVALL:
            return True
        else:
            return False
                
    def run_it(self, now=False):
        global LAST_RUN
        self.clear_table()
        for item_type in (models.URIT_OBJECT, models.URIT_SHOWNBY, models.URIT_SHOWNAT):
            self.base_counts(item_type)
            self.ok_counts(item_type)
            self.bad_counts(item_type)
            
        self.calc_stats()
        TimeStampLinkStats().set()
        LAST_RUN = time.time()
        return True
            
    def clear_table(self):
        cursor = connection.cursor()
        sql = "DELETE FROM plug_uris_reqstats;COMMIT"
        i = cursor.execute(sql)
        
    def base_counts(self, item_type):
        self.task_force_progress_timeout()
        self.task_starting('stats counts %i' % item_type)
        cursor = connection.cursor()
        sql = ["SELECT r.id,r.file_name,r.record_count, COUNT(ru.id) url_c"]
        sql.append("FROM plug_uris_uri u, plug_uris_requri ru, dummy_ingester_request r")
        sql.append("WHERE ru.req=r.id AND u.id=ru.uri AND u.item_type=%i" % item_type)
        sql.append("GROUP BY r.id,r.file_name,r.record_count")
        i = cursor.execute(" ".join(sql))
        self.task_set_nr_of_steps(cursor.rowcount)
        items = []
        idx = 0
        for req_id, file_name, record_count, url_count in cursor.fetchall():
            idx += 1
            try:
                request, created = models.ReqStats.objects.get_or_create(req_id=req_id)
            except:
                # will be triggered if table is empty - aargh...
                request = models.ReqStats()
                request.req_id = req_id
                created = True
            if created:
                request.file_name = file_name
            request.record_count = record_count
            if item_type == 1:
                request.count_1 = url_count
            elif item_type == 2:
                request.count_2 = url_count
            else:
                request.count_3 = url_count
            request.save()
            self.task_time_to_show(idx)
        self.task_progress(idx)
        

    def ok_counts(self, item_type):
        self.task_force_progress_timeout()
        self.task_starting('stats oks %i' % item_type)
        cursor = connection.cursor()
        sql = ["SELECT r.id, COUNT(u.id)"]
        sql.append("FROM plug_uris_uri u, plug_uris_requri ru, dummy_ingester_request r")
        sql.append("WHERE ru.req=r.id AND u.id=ru.uri AND u.item_type=%i" % item_type)
        sql.append("AND u.status=100 AND u.err_code=0")
        sql.append("GROUP BY r.id")
        i = cursor.execute(" ".join(sql))
        self.task_set_nr_of_steps(cursor.rowcount)
        idx = 0
        items = []
        for req_id, count_ok in cursor.fetchall():
            idx += 1
            try:
                request = models.ReqStats.objects.get(req_id=req_id)
            except:
                continue
            if item_type == 1:
                request.ok_1 = count_ok
            elif item_type == 2:
                request.ok_2 = count_ok
            else:
                request.ok_3 = count_ok
            request.save()
            self.task_time_to_show(idx)
        self.task_progress(idx)
            
    def bad_counts(self, item_type):
        self.task_force_progress_timeout()
        self.task_starting('stats bads %i' % item_type)
        cursor = connection.cursor()
        sql = ["SELECT r.id, COUNT(u.id)"]        
        sql.append("FROM plug_uris_uri u, plug_uris_requri ru, dummy_ingester_request r")
        sql.append("WHERE ru.req=r.id  AND u.id=ru.uri")
        sql.append("AND u.item_type=%i" % item_type)
        sql.append("AND u.err_code>0")
        sql.append("GROUP BY r.id")
        i = cursor.execute(" ".join(sql))
        self.task_set_nr_of_steps(cursor.rowcount)
        idx = 0
        items = []
        for req_id, count_bad in cursor.fetchall():
            idx += 1
            try:
                request = models.ReqStats.objects.get(req_id=req_id)
            except:
                continue
            if item_type == 1:
                request.bad_1 = count_bad
            elif item_type == 2:
                request.bad_2 = count_bad
            else:
                request.bad_3 = count_bad
            request.save()
            self.task_time_to_show(idx)
        self.task_progress(idx)
                        
            
    def calc_stats(self):
        self.task_force_progress_timeout()
        self.task_starting('stats',models.ReqStats.objects.count())
        idx = 0
        for rs in models.ReqStats.objects.all():
            idx += 1
            rs.waiting_1 = rs.count_1 - rs.ok_1 - rs.bad_1
            rs.ratio_1 = s_calc_ratio_bad(rs.ok_1, rs.bad_1)
            rs.waiting_2 = rs.count_2 - rs.ok_2 - rs.bad_2
            rs.ratio_2 = s_calc_ratio_bad(rs.ok_2, rs.bad_2)
            rs.waiting_3 = rs.count_3 - rs.ok_3 - rs.bad_3
            rs.ratio_3 = s_calc_ratio_bad(rs.ok_3, rs.bad_3)
            rs.save()
            self.task_time_to_show(idx)
        self.task_progress(idx)
                        
            
    def trigger_timeout(self):
        global LAST_RUN
        LAST_RUN = 0
        
    def not_s_calc_ratio(self, part, whole):
        s = u'%0.2f' % calc_ratio(part, whole)
        return s




def s_calc_ratio_bad(good, bad):
    s = u'%0.2f' % (100 - calc_ratio(bad, good+ bad))
    return s

def calc_ratio(part, whole):
    if not whole:
        # avoid divide by zero
        return 100
    f =  (part/float(whole)) * 100
    return f

