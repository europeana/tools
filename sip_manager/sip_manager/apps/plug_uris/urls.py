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

from django.conf.urls import *

from django.conf import settings

import views


urlpatterns = patterns('',

    url(r'^dg1/$', views.dg1, name='uri_dg1'),
    
    url(r'^stats_reqs/itmtype_(?P<item_type>\d+)/$', views.stats_req_lst, name='stats_req'),
    url(r'^stats_reqs/$', views.stats_req_summary, name='stats_req_summary'),
    url(r'^stats_by_reqs/req_(?P<sreq_pk>\d+)/itmtype_(?P<sitem_type>\d+)/$', views.stats_by_req, name='stats_by_req'),
    

    url(r'^stats_uri/(?P<order_by>\S+)/$', views.stats_by_uri, name='uri_stats'),
    url(r'^stats_uri/$', views.stats_by_uri, name='uri_stats'),
    
    url(r'^problems/(?P<source_id>\S+)/$', views.problems, name='uri_problems'),
    #url(r'^problems/$', views.problems, name='uri_problems'),

    url(r'^bad_by_req_e/req_(?P<req_pk>\d+)/itmtype_(?P<item_type>\d+)/err_(?P<err_code>\d+)/$', views.uri_bad_by_req_err, name='uri_bad_by_req_err'),
    url(r'^bad_by_req_s/req_(?P<req_pk>\d+)/itmtype_(?P<item_type>\d+)/webserv_(?P<webserver_id>\d+)/$', views.uri_bad_by_server, name='uri_bad_by_server'),
    url(r'^bad_by_req_m/req_(?P<req_pk>\S+)/itmtype_(?P<item_type>\d+)/mime_(?P<mime_type>\S+)/$', views.uri_bad_by_req_mime, name='uri_bad_by_req_mime'),


    url(r'^rescedule/req_(?P<req_pk>\d+)/itmtype_(?P<item_type>\d+)/label_(?P<filter_label>\S+)/errkey_(?P<err_key>\S+)/errval_(?P<err_value>\S+)/$', views.rescedule, name='uri_bad_rescedule'),
    # req_pk, item_type, filter_label, err_code, err_value

    # ej kollade
    url(r'^bad_by_req/(?P<offset>\d+)/$', views.uri_bad_by_request, name='uri_bad_by_request'),

)
