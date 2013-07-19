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


from decimal import Decimal, getcontext
import time
import urllib

from django.db.models import Q, Sum, Avg
from django.db import connection

from django.contrib.auth.decorators import login_required
from django.shortcuts import render_to_response, get_object_or_404


from utils.gen_utils import db_is_mysql
from utils.timestamp_file import TimeStampLinkStats


from apps.dummy_ingester.models import Request
from apps.base_item import models as base_item

#from datagrids import UriSourcesDataGrid
import models
from update_req_stats import s_calc_ratio_bad

Q_OBJECT = Q(item_type=models.URIT_OBJECT)
Q_OK = Q(status=models.URIS_COMPLETED, err_code=models.URIE_NO_ERROR)
Q_BAD = ~Q(err_code=models.URIE_NO_ERROR)


BAD_BY_REQ_PG_SIZE = 150
DUP_BY_REQ_PG_SIZE = 150


def dg1(request, template_name='plug_uris/datagrid1.html'):
    return UriSourcesDataGrid(request).render_to_response(template_name)

def summary():
    sums = {}
    rs = models.ReqStats.objects.all()
    for field in ('record_count', 
                  'ok_1', 'waiting_1', 'ratio_1',
                  'ok_2', 'waiting_2', 'ratio_2',
                  'ok_3', 'waiting_3', 'ratio_3',):
        sums[field] = rs.aggregate(Sum(field))['%s__sum' % field]
    return sums


def stats_req_summary(request):
    stat_time = TimeStampLinkStats().get()
    items = models.ReqStats.objects.all().order_by('file_name')
    sums = {}
    for field in ('record_count', 
                  'ok_1', 'waiting_1', 
                  'ok_2', 'waiting_2', 
                  'ok_3', 'waiting_3', ):
        sums[field] = items.aggregate(Sum(field))['%s__sum' % field]
    getcontext().prec = 4
    for field in ('ratio_1','ratio_2','ratio_3',):
        sums[field] = Decimal(items.aggregate(Avg(field))['%s__avg' % field]) + 0
        
    return render_to_response("plug_uris/stats_req_summary.html",
                              {
                                  'stats_update_time': stat_time,
                                  'items': items,
                                  'summary':sums,
                                  },)
    


def stats_req_lst(request, item_type):
    iitem_type = int(item_type)
    items = []
    #stat_records = 0
    #stat_links_found = 0
    #stat_waiting = 0
    #stat_bad_objs
    all_items = models.ReqStats.objects.all().order_by('file_name')
    for rs in all_items:
        itm = {'req_id': rs.req_id,
               'name': rs.file_name,
               'record_count': rs.record_count}
        if iitem_type == 1:
            itm['count'] = rs.count_1
            itm['ok'] = rs.ok_1
            itm['bad'] = rs.bad_1
            itm['waiting'] = rs.waiting_1
            itm['ratio'] = rs.ratio_1
        elif iitem_type == 2:
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
        items.append(itm)
        
    sums = {'record_count': all_items.aggregate(Sum('record_count'))['record_count__sum']}
    for field in ('count',
                  'ok',
                  'bad',
                  'waiting',
                  ):
        typed_field = '%s_%i' %(field, iitem_type)
        sums[field] = all_items.aggregate(Sum(typed_field))['%s__sum' % typed_field]
    getcontext().prec = 4
    for field in ('ratio',):
        typed_field = '%s_%i' %(field, iitem_type)
        sums[field] = Decimal(all_items.aggregate(Avg(typed_field))['%s__avg' % typed_field]) + 0
    return render_to_response("plug_uris/stats_all_requests.html",
                              {
                                  'stats_update_time': TimeStampLinkStats().get(),
                                  'items': items,
                                  'label': models.URI_TYPES[int(item_type)],
                                  'item_type': item_type,
                                  'summary':sums,
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


def stats_by_req(request, sreq_pk=0, sitem_type=-1):
    req_pk = int(sreq_pk)
    item_type = int(sitem_type)
    q_all = Q(req=req_pk, item_type=int(item_type))
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


def uri_bad_by_req_err(request, req_pk, item_type, err_code):
    request.session['req_filter'] = {'key': 'err_code',
                                     'item_type': int(item_type),
                                     'value': err_code,
                                     'req_pk': req_pk,
                                     'filter_label':'error',
                                     }
    return uri_bad_by_request(request)

def uri_bad_by_server(request, req_pk, item_type, webserver_id):
    request.session['req_filter'] = {'key': 'uri_source',
                                     'item_type': int(item_type),
                                     'value': webserver_id,
                                     'req_pk': req_pk,
                                     'filter_label': 'webserver',
                                     }
    return uri_bad_by_request(request)


def uri_bad_by_req_mime(request, req_pk, item_type, mime_type):
    request.session['req_filter'] = {'key': 'mime_type',
                                     'item_type': int(item_type),
                                     'value': mime_type,
                                     'req_pk': req_pk,
                                     'filter_label':'mime-type',
                                     }
    return uri_bad_by_request(request)


def uri_bad_by_request(request, offset=0):
    offset = int(offset)
    sel = request.session['req_filter']
    req = models.Request.objects.get(pk=sel['req_pk'])
    q_selection = Q((sel['key'], sel['value']), req=sel['req_pk'])
    #
    #
    #
    qs = models.Uri.objects.filter(q_selection, Q(item_type=sel['item_type']), Q_BAD)
    problems = []
    for uri in qs[ offset : offset+BAD_BY_REQ_PG_SIZE ]:
        problems.append({'url': uri.url,
                         'uri_id': uri.pk,
                         'status': models.URI_STATES[uri.status],
                         'errname': models.URI_ERR_CODES[uri.err_code],
                         'err_msg': uri.err_msg,
                         })

    item_count = qs.count()
    pages = item_count / BAD_BY_REQ_PG_SIZE + 1
    return render_to_response("plug_uris/bad_by_request.html",
                              {
                                  'request': request,
                                  'req': req,
                                  #'mime_type': mime_type,
                                  'problems': problems,

                                  'prev': max(offset - BAD_BY_REQ_PG_SIZE, 0),
                                  'next': offset + BAD_BY_REQ_PG_SIZE,
                                  'last': max(0, item_count - BAD_BY_REQ_PG_SIZE),
                                  'pages': pages,
                                  'item_count': item_count,
                                  'req_pk': sel['req_pk'],
                                  'item_type':sel['item_type'],
                                  'filter_label': sel['filter_label'],
                                  'err_key': sel['key'],
                                  'err_value': sel['value'],
                              },
                              )

@login_required
def rescedule(request, req_pk, item_type, filter_label, err_key, err_value):
    req = models.Request.objects.get(pk=req_pk)

    cursor = connection.cursor()

    if db_is_mysql:
        sql = ["UPDATE %s ru, %s u" % (models.TBL_REQURI, models.TBL_URIS)]
        
        sql.append("SET ru.mime_type='', ru.file_type=''")
        sql.append(", ru.status=%i" % models.URIS_CREATED)
        sql.append(", ru.err_code=%i" % models.URIE_NO_ERROR)
        
        sql.append(", u.mime_type='',u.file_type=''")
        sql.append(", u.file_type=''")
        sql.append(", u.org_w=0,u.org_h=0")
        sql.append(", u.pid=0")
        sql.append(", u.url_hash=''")
        sql.append(", u.content_hash=''")
        sql.append(", u.status=%i" % models.URIS_CREATED)
        sql.append(", u.err_code=%i,u.err_msg=''" % models.URIE_NO_ERROR)
        
        sql.append("WHERE u.id=ru.uri")
        sql.append("AND ru.req=%i" % int(req_pk))
        sql.append("AND ru.item_type=%i" % int(item_type))
        sql.append("AND ru.%s=%i ;commit" % (err_key, int(err_value)))
        i = cursor.execute(" ".join(sql))
        item_count = int(i/2)
    else:
        
        l = ["WHERE req=%i" % int(req_pk)]
        l.append("AND item_type=%i" % int(item_type))
        if isinstance(err_value, basestring):
            l.append("AND %s='%s'" % (err_key,err_value))
        else:
            l.append("AND %s=%s" % (err_key,err_value))
        requri_where = " ".join(l)
        
        sql = ["UPDATE plug_uris_uri "]

        sql.append("SET mime_type='',file_type=''")
        sql.append(", org_w=0, org_h=0")
        sql.append(", pid=0")
        sql.append(", url_hash=''")
        sql.append(", content_hash=''")
        sql.append(", status=%i" % models.URIS_CREATED)
        sql.append(", err_code=%i, err_msg=''" % models.URIE_NO_ERROR)
        sql.append("WHERE req=%i" % int(req_pk))
        sql.append("AND item_type=%i" % int(item_type))
        if isinstance(err_value, basestring):
            sql.append("AND %s='%s'" % (err_key,err_value))
        else:
            sql.append("AND %s=%s" % (err_key,err_value))
        sql.append("AND err_code != 0") # only rescedle things that are broken
        i = cursor.execute(" ".join(sql))
        item_count = cursor.rowcount
        i = cursor.execute("COMMIT")
        
        
    return render_to_response("plug_uris/bad_resceduled.html",
                              {
                                  'request': request,
                                  'req': req,
                                  'filter_label': filter_label,
                                  'item_count': item_count,
                                  },
                              )









## Not verified




def problems(request, source_id=-1):
    try:
        urisource = models.UriSource.objects.get(pk=source_id)
    except:
        urisource = None
    problems = {}
    for k in models.URI_ERR_CODES:
        if k == models.URIE_NO_ERROR:
            continue
        uri_filter = {'err_code': k}
        if urisource:
            uri_filter['uri_source'] = urisource.pk
        count = models.Uri.objects.filter(**uri_filter).count()
        if not count:
            continue
        problems[models.URI_ERR_CODES[k]] = {
            'err_code': k,
            'count': count,
        }
    return render_to_response('plug_uris/problems.html',
                              {
                                  'urisource': urisource,
                                  'problems': problems},
                              )






#
# Util funcs
#


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


