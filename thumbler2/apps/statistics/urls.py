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

from django.conf.urls import patterns, include, url


import views



urlpatterns = patterns('',
    url(r'^$', views.stats_summary, name='stats_summary'),

    url(r'^by_dataset/(?P<ds_pk>\d+)/$', views.stats_by_ds, name='stats_by_ds'),
    url(r'^errors_by_err_code/(?P<ds_pk>\d+)/(?P<err_code>\d+)/$', views.uri_bad_by_err_code, name='errors_by_err_code'),
    url(r'^errors_by_mime_type/(?P<ds_pk>\d+)/(?P<mime_type>\d+)/$', views.uri_bad_by_mime_type, name='errors_by_mime_type'),
    url(r'^errors_by_webserver/(?P<ds_pk>\d+)/(?P<uri_source>\d+)/$', views.uri_bad_by_webserver, name='errors_by_webserver'),
    url(r'^bad_items_paging/(\d+)/$',  views.uri_bad_items_pager, name='bad_items_paging'),
    url(r'^rescedule/$', views.rescedule, name='uri_bad_rescedule'),

    url(r'^webservers_waiting/$', views.webservers_waiting, name='webservers_waiting'),
    url(r'^one_webserv_status/(\d+)/$', views.one_webserver_stats, name='one_webserv_status'),
  )
