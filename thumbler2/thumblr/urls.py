from django.conf.urls import patterns, include, url
from django.shortcuts import render_to_response

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

def top_index(request):
    return render_to_response("top_index.html", {'request': request,})

def logout_view(request):
    logout(request)
    return redirect('top_index')

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'thumblr.views.home', name='home'),
    # url(r'^thumblr/', include('thumblr.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),
    
    url(r'^$', top_index, name='top_index'),
    url(r'^logfile/$', 'apps.sipmanager.views.logfile', name='logfile'),
    
    (r'^uris/', include('apps.plug_uris.urls')),
    (r'^sipm/', include('apps.sipmanager.urls')),
    (r'^stats/', include('apps.statistics.urls')),
    #(r'^optout/', include('apps.optout.urls')),    
    
)
