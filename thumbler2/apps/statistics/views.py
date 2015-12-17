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

from django.db.models import Sum, Avg, Q, Count
from django.db.models.query import QuerySet
from django.db import connection

from django.contrib.auth.decorators import login_required
from django.shortcuts import render_to_response, get_object_or_404

from utils.timestamp_file import TimeStampLinkStats

import apps.plug_uris.models as uri_models

import models


BAD_BY_REQ_PG_SIZE = 150


PRSP_WEBSERVER = 2
PRSP_ERRCODE = 3
PRSP_MIME = 4



class NumericMime(object):
    def __init__(self):
        self.data_by_mime = {}
        self.data_by_idx = {}
        self.idx = 0
        
    def mime_to_int(self, s):
        if not self.data_by_mime.has_key(s):
            self.idx+=1
            self.data_by_mime[s] = self.idx
            self.data_by_idx[self.idx] = s
        return self.data_by_mime[s]

    def int_to_mime(self, i):
        if self.data_by_idx.has_key(i):
            s = self.data_by_idx[i]
        else:
            s = ''
        return s
    
mimenum = NumericMime()





def webservers_waiting(request):
    webservers = models.WebserverStats.objects.all().order_by('eta')
    return render_to_response("statistics/stats_all_webservers.html",
                              {
                                  'stats_update_time': TimeStampLinkStats().get(),
                                  'webservers': webservers,
                                  },)


def one_webserver_stats(request, uri_source):
    iuri_source = int(uri_source)
    cursor = connection.cursor()
    sql = ['select d.id, ffile, count(ffile) as items from plug_uris_uri u, dataset_dataseturls du, dataset_dataset d']
    sql.append('where uri_source=%i' % iuri_source)
    sql.append('and du.uri_id = u.id and d.id = du.ds_id')
    sql.append('group by d.id, ffile order by items desc')
    cursor.execute(" ".join(sql))
    datasets = []
    for ds_id, ds_name, count in cursor.fetchall():
        q = Q(dataseturls__ds_id__exact=ds_id,uri_source=iuri_source)
        web_servers, summary = stats_by_webserver(q)
        
        try:
            ratio = 100 * summary['good'] / float(count - summary['waiting']) 
        except:
            ratio = 0.0
        
        datasets.append({'ds_id':ds_id, 'name':ds_name,'item_count':count,'bad':summary['bad'],'good':summary['good'],
                         'waiting':summary['waiting'],'ratio':ratio})
        
    webserver = uri_models.UriSource.objects.get(pk=iuri_source).name_or_ip
    return render_to_response("statistics/stats_one_webservers.html", {'datasets': datasets,
                                                                       'webserver': webserver,
                                                                       'uri_source': iuri_source},)
    
  


def stats_summary(request, item_type=1):
    items = models.Statistics.objects.all().order_by('set_name')
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
    
    
    return render_to_response("statistics/stats_all_items.html",
                              {
                                  'stats_update_time': TimeStampLinkStats().get(),
                                  'items': items,
                                  'summary': summary,
                                  },)



def stats_by_ds(request,ds_pk):
    
    ids = int(ds_pk)
    q_ds = Q(dataseturls__ds_id__exact=ids,)
    mime_types = stats_by_mime_type(q_ds)
    err_by_reasons = stats_by_error(q_ds)
    webservers, webs_summary = stats_by_webserver(q_ds)

    return render_to_response("statistics/stats_by_request.html",
                              {
                                  'ds_id': ids,
                                  'mime_types': mime_types,
                                  'err_by_reasons': err_by_reasons,
                                  'webservers': webservers,
                                  'webservers_summary': webs_summary,
                                  },
                              )



def uri_bad_by_mime_type(request, ds_pk, mime_type):
    ids = int(ds_pk)
    imime= int(mime_type)
    request.session['bad_item_pager'] = {'ds_pk':ids,
                                         'perspective':PRSP_MIME,
                                         'persp_param': imime,
                                         'title':'Bad by MIME type',
                                         'subtitle': mimenum.int_to_mime(imime)}
    return uri_bad_items_pager(request, 0)
    
def uri_bad_by_err_code(request, ds_pk, err_code):
    ids = int(ds_pk)
    ierr= int(err_code)
    request.session['bad_item_pager'] = {'ds_pk':ids,
                                         'perspective':PRSP_ERRCODE,
                                         'persp_param': ierr,
                                         'title':'Bad by error',
                                         'subtitle': uri_models.URI_ERR_CODES[ierr] }
    return uri_bad_items_pager(request, 0)

def uri_bad_by_webserver(request, ds_pk, uri_source):
    ids = int(ds_pk)
    iuri_source= int(uri_source)
    request.session['bad_item_pager'] = {'ds_pk':ids,
                                         'perspective':PRSP_WEBSERVER,
                                         'persp_param': iuri_source,
                                         'title':'Bad by webserver',
                                         'subtitle': 'websever: %s' % uri_models.UriSource.objects.get(pk=iuri_source).name_or_ip
                                         }
    return uri_bad_items_pager(request, 0)

def uri_bad_items_pager(request, offset):
    offset=int(offset)
    ses = request.session['bad_item_pager']
    q = get_q_by_perspective(ses)
    items = q.values('pk','status','url','err_msg')[offset:offset+BAD_BY_REQ_PG_SIZE]
    if not ses.has_key('item_count'):
        ses['item_count'] = q.count()
        ses['page_count'] = int(ses['item_count'] / BAD_BY_REQ_PG_SIZE) + 1
    nav = {'prev': max(offset - BAD_BY_REQ_PG_SIZE, 0),
           'next': max(min(offset + BAD_BY_REQ_PG_SIZE, ses['item_count'] - BAD_BY_REQ_PG_SIZE), 0), 
           'last': max(ses['item_count'] - BAD_BY_REQ_PG_SIZE, 0),}
    return render_to_response("statistics/bad_by_request.html",
                              {'ses': ses, 'items': items, 'nav': nav,},)


@login_required
def rescedule(request):
    ses = request.session['bad_item_pager']
    q = get_q_by_perspective(ses)
    
    affected_rows = q.update(
        status=uri_models.URIS_CREATED,
        mime_type='',
        file_type='',
        org_w=0, org_h=0,
        pid=0,
        url_hash='', content_hash='',
        err_code=uri_models.URIE_NO_ERROR, err_msg='',)
    return render_to_response("statistics/bad_resceduled.html",
                              {
                                  'ses': ses,
                                  'affected_rows': affected_rows,
                                  },
                              )






#======================= internals =======
def get_q_by_perspective(ses):
    q_ds = uri_models.Uri.objects.filter(dataseturls__ds_id__exact=ses['ds_pk'],)
    perspective = ses['perspective']
    if ses['perspective'] == PRSP_MIME:
        q2 = q_ds.filter(mime_type=mimenum.int_to_mime(ses['persp_param']))
    elif ses['perspective'] == PRSP_ERRCODE:
        q2 = q_ds.filter(err_code=ses['persp_param'])
    elif ses['perspective'] == PRSP_WEBSERVER:
        q2 = q_ds.filter(uri_source=ses['persp_param']).exclude(err_code=uri_models.URIE_NO_ERROR)
    return q2


def stats_by_webserver(q):
    web_servers = uri_models.Uri.objects.filter(q).values('uri_source').distinct().annotate(total=Count('uri_source'))
    summary = {
        'total': 0,
        'good': 0,
        'waiting': 0,
        'bad': 0, }
        
    for itm in web_servers:
        try:
            s = uri_models.UriSource.objects.get(pk=itm['uri_source']).name_or_ip
        except:
            s = 'webserver missing'
        itm['name_or_ip'] = s
        summary['total'] += itm['total']
        itm['good'] = uri_models.Uri.objects.filter(q, uri_source=itm['uri_source'],status=uri_models.URIS_COMPLETED).count()
        summary['good'] += itm['good']
        itm['waiting'] = uri_models.Uri.objects.filter(q, uri_source=itm['uri_source'],err_code=uri_models.URIE_NO_ERROR).exclude(
            status=uri_models.URIS_COMPLETED).count()
        summary['waiting'] += itm['waiting']
        itm['bad'] = itm['total'] - itm['good'] - itm['waiting']
        summary['bad'] += itm['bad']
        try:
            r = 100 * float(itm['good']) / (itm['total'] - itm['waiting'])
        except:
            r = 0.0
        itm['ratio'] = r

    return web_servers, summary
    

def stats_by_error(q):
    #
    # Grouped by error
    #
    errors = uri_models.Uri.objects.filter(q).exclude(err_code=uri_models.URIE_NO_ERROR).values('err_code').distinct().annotate(count=Count('err_code'))
    for itm in errors:
        itm['label'] = uri_models.URI_ERR_CODES[itm['err_code']]
    return errors
    

def stats_by_mime_type(q):
    #
    # Grouped by mimetype
    #
    #.values_list('style_id', flat=True).distinct()
    mime_types = uri_models.Uri.objects.filter(q).exclude(mime_type='').values('mime_type').distinct().annotate(total=Count('mime_type'))
    for itm in mime_types:
        mime_type = itm['mime_type']
        itm['mime_code'] = mimenum.mime_to_int(mime_type)
        #a = Uri.objects.filter(q_all,mime_type=mime_type,).values('status').annotate(dcount=Count('status'))
        good = uri_models.Uri.objects.filter(q, mime_type=mime_type,status=uri_models.URIS_COMPLETED).count()
        bad = itm['total'] - good
        try:
            ratio = 100 * float(good) / itm['total']
        except:
            ratio = 0.0
        itm['good'] = good
        itm['bad'] = bad
        itm['ratio']= ratio
    return mime_types

