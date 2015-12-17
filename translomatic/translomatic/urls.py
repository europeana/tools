from django.conf.urls import include, url
from django.contrib import admin
from django.conf import settings
from django.conf.urls.static import static


urlpatterns = [
    # Examples:
    # url(r'^$', 'translomatic.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),

    url(r'^admin/', include(admin.site.urls)),

    url(r'^rosetta/', include('rosetta.urls')),

    # portal request comes in either as /page.html or /portal/page.html
    url(r'^$', include('apps.portal_trans.urls')),
    url(r'^portal/', include('apps.portal_trans.urls')),
    url(r'^(?P<rel_url>.*)$', 'apps.portal_trans.views.portal_url'),

    #static(settings.STATIC_URL, document_root=settings.STATIC_ROOT),
]

