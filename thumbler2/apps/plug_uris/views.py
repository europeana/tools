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

from django.shortcuts import render_to_response, get_object_or_404


from utils.gen_utils import db_type
from utils.timestamp_file import TimeStampLinkStats


#from apps.dummy_ingester.models import Request

#from datagrids import UriSourcesDataGrid
import models

Q_OBJECT = Q(item_type=models.URIT_OBJECT)
Q_OK = Q(status=models.URIS_COMPLETED, err_code=models.URIE_NO_ERROR)
Q_BAD = ~Q(err_code=models.URIE_NO_ERROR)


DUP_BY_REQ_PG_SIZE = 150



def dg1(request, template_name='plug_uris/datagrid1.html'):
    return UriSourcesDataGrid(request).render_to_response(template_name)








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






