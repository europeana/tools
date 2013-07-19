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

# Create your views here.

from django.conf import settings
from decimal import Decimal, getcontext

from django.db.models import Sum, Avg
from django.shortcuts import render_to_response, get_object_or_404

from utils.timestamp_file import TimeStampLinkStats

import models


def logfile(request):
    fp = open(settings.SIP_LOG_FILE)
    lst = fp.readlines()
    fp.close()
    lst = lst[-250:] # trim logfile
    s = ''.join(lst).replace('\n','<br>').replace('\t','&nbsp;&nbsp;&nbsp;')
    return HttpResponse("Content of logfile<br>%s" % s)




def stats_req_summary(request):
    stat_time = TimeStampLinkStats().get()
    items = models.Statistics.objects.all().order_by('set_name')
    getcontext().prec = 4
    summary = {}
    for field in ('bad_1',
                  'count_1',
                  'ok_1',
                  'ratio_1',
                  'record_count',
                  'waiting_1',
                  ):
        if field in ('ratio_1',):
            summary[field] = Decimal(items.aggregate(Avg(field))['%s__avg' % field]) + 0
        else:
            summary[field] = items.aggregate(Sum(field))['%s__sum' % field]

    return render_to_response("statistics/stats_req_summary.html",
                              {
                                  'stats_update_time': stat_time,
                                  'items': items,
                                  'summary': summary,
                                  },)
    


def stats_req_lst(request, item_type):
    return render_to_response("plug_uris/stats_all_requests.html",
                              {
                                  'stats_update_time': TimeStampLinkStats().get(),
                                  'items': filtered_reqstats(int(item_type)),
                                  'label': models.URI_TYPES[int(item_type)],
                                  'item_type': item_type,
                                  },)

def stats_by_uri(request, order_by=''):
    p_order_by = 'name_or_ip'
    request.session['sortkey'] = p_order_by

    uri_sources = numeric_req_stats(0,webservers=True)
    return render_to_response("plug_uris/stats_uri_source.html",
                              {
                                  "uri_sources":uri_sources,
                                  "summary": uri_summary(models.URIT_OBJECT),},
                              )


def stats_by_ds(request, sds_pk):
    ds_pk = int(sds_pk)
    q_all = Q(ds=ds_pk,)
    qs_all = models.Uri.objects.filter(q_all)

    #
    # Grouped by mimetype
    #
    mime_results = numeric_req_stats(item_type, req_id=req_pk,mime_check=True)
    for mt in mime_results:
        mt['mime_url'] = urllib.quote_plus(mt['name'])
    #
    # Grouped by error
    #
    err_by_reasons = []
    for err_code in models.URI_ERR_CODES.keys():
        if err_code == models.URIE_NO_ERROR:
            continue
        count = qs_all.filter(err_code=err_code).count()
        if not count:
            continue
        err_by_reasons.append({'err_code' : err_code,
                               'err_msg': models.URI_ERR_CODES[err_code],
                               'count': count})
    #
    # Grouped by webserver
    #
    webservers = numeric_req_stats(item_type, req_pk)
    tot_items = tot_good = tot_bad = tot_waiting = 0
    for ws in webservers:
        tot_items += ws['count']
        tot_good += ws['ok']
        tot_bad += ws['bad']
        tot_waiting += ws['waiting']

    request = models.Request.objects.filter(pk=req_pk)[0]
    
    return render_to_response("plug_uris/stats_by_request.html",
                              {
                                  'request': request,
                                  'mime_results': mime_results,
                                  'err_by_reasons': err_by_reasons,
                                  'item_type': item_type,
                                  'webservers': webservers,
                                  'webservers_summary': {
                                      'count': tot_items,
                                      'waiting': tot_waiting,
                                      'good': tot_good,
                                      'bad': tot_bad,},
                                  },
                              )






#
# Util funcs
#
def filtered_reqstats(item_type):
    lst = []
    for rs in models.ReqStats.objects.all().order_by('file_name'):
        itm = {'req_id': rs.req_id,
               'name': rs.file_name,
               'record_count': rs.record_count}
        if item_type == 1:
            itm['count'] = rs.count_1
            itm['ok'] = rs.ok_1
            itm['bad'] = rs.bad_1
            itm['waiting'] = rs.waiting_1
            itm['ratio'] = rs.ratio_1
        elif item_type == 2:
            itm['count'] = rs.count_2
            itm['ok'] = rs.ok_2
            itm['bad'] = rs.bad_2
            itm['waiting'] = rs.waiting_2
            itm['ratio'] = rs.ratio_2
        else:
            itm['count'] = rs.count_3
            itm['ok'] = rs.ok_3
            itm['bad'] = rs.bad_3
            itm['waiting'] = rs.waiting_3
            itm['ratio'] = rs.ratio_3
        lst.append(itm)
    return lst


def numeric_req_stats(item_type=-1,req_id=0,mime_check=False,webservers=False):
    cursor = connection.cursor()
    items = {}
    prim_key_label = 'req_id'
    if mime_check:
        # all mimes one request
        sql1 = "SELECT 100, mime_type, COUNT(id), count(id) FROM plug_uris_uri WHERE req=%i AND item_type=1 AND mime_type <>'' GROUP BY mime_type" % req_id
        sql2 = "SELECT mime_type, COUNT(id) FROM plug_uris_uri WHERE req=%i AND item_type=1 AND status=100 AND err_code=0 AND mime_type <>'' GROUP BY mime_type" % req_id
        sql3 = "SELECT mime_type, COUNT(id) FROM plug_uris_uri WHERE req=%i AND item_type=1 AND err_code>0 AND mime_type <>'' GROUP BY mime_type" % req_id
    elif webservers:
        # all webservers all requests
        prim_key_label = 'id'
        sql1 = "SELECT s.id,s.name_or_ip, COUNT (u.id), COUNT (u.id) FROM plug_uris_uri u, plug_uris_urisource s WHERE s.name_or_ip<>'' AND s.id=u.uri_source GROUP BY s.id, s.name_or_ip, s.id"
        sql2 = "SELECT s.name_or_ip, COUNT (u.id) FROM plug_uris_uri u, plug_uris_urisource s WHERE s.name_or_ip<>'' AND s.id=u.uri_source AND u.status=100 AND err_code=0 GROUP BY s.name_or_ip"
        sql3 = "SELECT s.name_or_ip, COUNT (u.id) FROM plug_uris_uri u, plug_uris_urisource s WHERE s.name_or_ip<>'' AND s.id=u.uri_source AND err_code>0 GROUP BY s.name_or_ip"
    elif req_id:
        # all webservers one request
        sql = "COUNT (u.id) FROM plug_uris_uri u, dummy_ingester_request r, plug_uris_urisource s WHERE r.id=%i AND u.req=r.id AND s.id=u.uri_source AND item_type=%i" % (req_id, item_type)
        sql1 = "SELECT s.id,s.name_or_ip, r.record_count, %s GROUP BY s.name_or_ip, s.id, r.record_count" % sql
        sql2 = "SELECT s.name_or_ip, %s AND u.status=100 AND err_code=0 GROUP BY s.name_or_ip" % sql
        sql3 = "SELECT s.name_or_ip, %s AND err_code>0 GROUP BY s.name_or_ip" % sql
    else: # not fixed to new db format
        sql = "COUNT(ru.id) FROM plug_uris_requri ru, dummy_ingester_request r WHERE ru.req=r.id AND item_type=%i" % item_type
        sql1 = "SELECT r.id,r.file_name,r.record_count,%s GROUP BY r.id,r.file_name,r.record_count" % sql
        sql2 = "SELECT r.file_name,%s AND ru.status=100 AND err_code=0 GROUP BY r.file_name" % sql
        sql3 = "SELECT r.file_name,%s AND err_code>0 GROUP BY r.file_name" % sql
        
    cursor.execute(sql1)
    for req_id, name, record_count, itm_count in cursor.fetchall():
        items[name] = {prim_key_label:req_id,'record_count':record_count,'count':itm_count,'ok':0,'bad':0}
        
    cursor.execute(sql2)
    for name, itm_ok in cursor.fetchall():
        items[name].update({'ok':itm_ok})
    cursor.execute(sql3)
    for name, itm_bad in cursor.fetchall():
        items[name].update({'bad':itm_bad})
    
    lst = []
    sorted_keys = items.keys()
    sorted_keys.sort()
    for req_name in sorted_keys:
        d = items[req_name]
        d['name'] = req_name
        d['waiting'] = d['count'] - d['ok'] - d['bad']
        d['ratio'] = s_calc_ratio_bad(d['ok'], d['bad'])
        lst.append(d)
    return lst


    
def uri_summary(item_type):
    itms_all =  models.Uri.objects.filter(item_type=item_type).count()
    itms_done = models.Uri.objects.filter(item_type=item_type,status=models.URIS_COMPLETED, err_code=models.URIE_NO_ERROR).count()
    itms_bad =  models.Uri.objects.filter(item_type=item_type,err_code__gt=models.URIE_NO_ERROR).count()
    return {'count': itms_all,
            'ok': itms_done,
            'bad': itms_bad,
            'waiting': itms_all - itms_done - itms_bad,
            }


