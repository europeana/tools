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

import time
import urllib

from django.db.models import Q, Sum
from django.db import connection

from django.contrib.auth.decorators import login_required
from django.shortcuts import render_to_response, get_object_or_404


from utils.gen_utils import db_type
from utils.timestamp_file import TimeStampLinkStats


#from apps.dummy_ingester.models import Request

#from datagrids import UriSourcesDataGrid
import models

Q_OBJECT = Q(item_type=models.URIT_OBJECT)
Q_OK = Q(status=models.URIS_COMPLETED, err_code=models.URIE_NO_ERROR)
Q_BAD = ~Q(err_code=models.URIE_NO_ERROR)


BAD_BY_REQ_PG_SIZE = 150
DUP_BY_REQ_PG_SIZE = 150

def dg1(request, template_name='plug_uris/datagrid1.html'):
    return UriSourcesDataGrid(request).render_to_response(template_name)

def foo():
    hepp = {}
    rs = models.ReqStats.objects.all()
    for field in ('record_count', 'count_1', 'ok_1','bad_1','waiting_1',
                  'count_2', 'ok_2','bad_2','waiting_2',
                  'count_3', 'ok_3','bad_3','waiting_3',):
        hepp[field] = rs.aggregate(Sum(field))['%s__sum' % field]
    return hepp



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

    if db_type == 'postgres':
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
    else:
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






