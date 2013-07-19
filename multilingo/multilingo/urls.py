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

 Initial release: 2010-02-05
 Version 1.1 2010-06-09
"""


from django.conf.urls.defaults import *
from django.conf import settings

from django.contrib import admin
admin.autodiscover()

from apps.multi_lingo import views





urlpatterns = patterns('',
    url(r'^$', views.index_page, name='top_index'),
)


if settings.DELIVER_STATIC_MEDIA:
    media_url = settings.MEDIA_URL
    # remove starting and ending slash from media_url
    if media_url[0] == '/':
        media_url = media_url[1:]
    if media_url[-1] == '/':
        media_url = media_url[:-1]

    #
    #  Handling of static files from within Django
    #
    
    # portal extras
    for s in ('js', 'css', 'images'):
        urlpatterns += patterns('django.views.static',
                            (r'^portal/%s/(?P<path>.*)$' % s,
                             'serve', {
                                 'document_root': '%s/%s' % (settings.MEDIA_ROOT,s),
                                 'show_indexes': True }),)
    # multilingo extras
    for s in ('js', 'css', 'img'):
        urlpatterns += patterns('django.views.static',
                                (r'^portal/%s/%s/(?P<path>.*)$' % (settings.MEDIA_FILE_PATH,s),
                                 'serve', {
                                     'document_root': '%s/%s/%s' % (settings.MEDIA_ROOT, settings.MEDIA_FILE_PATH, s),
                                     'show_indexes': True }),)

    urlpatterns += patterns('django.views.static',
                            (r'^portal/images/%s/(?P<path>.*)$' % settings.MEDIA_FILE_PATH,
                             'serve', {
                                 'document_root': '%s/%s/%s' % (settings.MEDIA_ROOT, settings.MEDIA_FILE_PATH, s),
                                 'show_indexes': True }),)
    """
    urlpatterns += patterns('django.views.static',
                            (r'^%s/(?P<path>.*)$' % media_url,
                             'serve', {
                                 'document_root': settings.MEDIA_ROOT,
                                 'show_indexes': True }),)
    """


urlpatterns += patterns(
    '',
    (r'^portal/(?P<rel_url>.*)$', views.portal_url),)

# Uncomment the admin/doc line below and add 'django.contrib.admindocs'
# to INSTALLED_APPS to enable admin documentation:
urlpatterns += patterns(
    '',
    (r'^admin/doc/', include('django.contrib.admindocs.urls')),)

# Uncomment the next line to enable the admin:
urlpatterns += patterns(
    '',
    url(r'^admin/', include(admin.site.urls), name='admin'),)


urlpatterns += patterns(
    '',
    (r'^submit/$',  views.submit_pages),)


urlpatterns += patterns('',
                        (r'^rosetta/', include('rosetta.urls')),
                        )
