from django.conf.urls import url

from . import views

urlpatterns = [
    url(r'^$', views.index_page, name='top_index'),
    url(r'^(?P<rel_url>.*)$', views.portal_url),
]