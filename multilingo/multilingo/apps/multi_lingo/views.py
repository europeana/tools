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

import os
import sys

import multilingo.settings as sett2

from django.conf import settings
from django.shortcuts import render_to_response, redirect
from django.http import HttpResponseRedirect
from django.core.urlresolvers import reverse


import django.template.loader

from rosetta.poutil import find_pos

from dataexp import get_tarball

from multilingo.apps.multi_lingo.submit_save import SubmitSaver

import models
from utils import SubmitError

HTML_EXT = '.html'
PROP_URL_NAME = 'message_keys/messages'
PROP_TEMPLATE = 'prop_file.html'
LANG_KEY = 'django_language'



# some caching
PORTAL_PREFIX = settings.PORTAL_PREFIX


def index_page(request):
    page_lst = models.OUR_STATIC_PAGES.keys()
    page_lst.sort()
    return render_to_response('list_all_pages.html',
                              {
                                  'portal_prefix': PORTAL_PREFIX,
                                  'pages': page_lst,
                              })




def portal_url(request, rel_url, lang='', *args, **kwargs):
    """Due to the way the official portal handles urls, everything starts with
    /portal/
    This is a bit counter-intuitive when handled by django, we filter out
    the pages we want to handle ourself, and redirect everything else to the
    static page handler
    """
    if rel_url == 'index.html':
        return index_page(request) # point back to topindex

    if rel_url in models.OUR_STATIC_PAGES.keys():
        return our_static_pages_handler(request, rel_url)
    stat_path = os.path.join(settings.MEDIA_URL, rel_url)
    #print '***', stat_path
    return HttpResponseRedirect(stat_path)




def submit_pages(request):
    global GIT_COMMIT_INITIATED_BY
    if not request.user.is_superuser:
        return render_to_response('admin/submit.html',
                                  {
                                      'request':  request,
                                      'error_msg': 'You are not superuser!!!',
                                  })
    
    if not request.user.email:
        return render_to_response('admin/submit.html',
                                  {
                                      'request':  request,
                                      'error_msg': 'no email defined for user: %s' % request.user,
                                  })
    try:
        submitter = SubmitSaver(request)
        submitter.run()
    except SubmitError as e:
        msg = 'Failed to submit:\n %s' % e.value
        return render_to_response('admin/submit.html',
                                  {
                                      'request':  request,
                                      'error_msg': msg,
                                  })
    except:
        e=sys.exc_info()
        try:
            if len(e):
                msg = 'Failed to submit:\n %s\n%s' % (e[1].__doc__, ' '.join(e[1].args))
            else:
                msg = 'Failed to submit, unable to parse exception: %s' % e
        except:
            msg = 'Unidentified failure when submitting'
        return render_to_response('admin/submit.html',
                                  {
                                      'request':  request,
                                      'error_msg': msg,
                                  })
        
    return render_to_response('admin/submit.html',
                              {
                                  'request':  request,
                                  'notification': 'Mail will be sent',
                              })



#=================   utils   =====================

def our_static_pages_handler(request, rel_url):
    template = models.OUR_STATIC_PAGES[rel_url]
    lang = request.session.get(LANG_KEY,'en')
    new_lang = request.POST.get('lang') or request.GET.get('lang')
    if not (lang or new_lang):
        new_lang = 'en'
    if new_lang:
        return set_lang_redirect(request, new_lang, rel_url)
    try:
        lang_long_name = settings.LANGUAGES_DICT[lang]
    except:
        return set_lang_redirect(request, 'en', rel_url)
    all_languages = settings.LANGUAGES
    europeana_item_count_mill = 22
    request_path = request.path
    return render_to_response(template, locals())


def set_lang_redirect(request, lang, next_page='/'):
    request.session[LANG_KEY] = lang
    if next_page[:6] != 'portal':
        next_page = os.path.join('/portal',next_page)
    return HttpResponseRedirect(next_page)


def prepare_generic_vars(lang):
    """
    Set up a few generic variables for usage in templates
    """
    if lang not in settings.LANGUAGES_DICT.keys():
        # trigger redirect to enforce nice language specific urls
        lang = 'en'
    return {'lang': lang,
            'lang_long_name': settings.LANGUAGES_DICT[lang],
            'all_languages': settings.LANGUAGES,
            'europeana_item_count_mill': '14,6',
            'request_path' : requ
            }






#=================   Finding templates   ======================

if sys.argv[-1] != 'syncdb':
    models.update_template_list() # do an initial scan on startup
