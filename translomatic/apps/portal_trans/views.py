import os

from django.shortcuts import render
from django.conf import settings


# Create your views here.

from .models import TranslatePage



LANG_KEY = 'translomatic_lang'

def index_page(request):
    page_lst = []
    for tp in TranslatePage.objects.all():
        page_lst.append( os.path.split(tp.file_name.name)[1])

    return render(request, 'portal_trans/list_all_pages.html',
                  {
                      #'portal_prefix': PORTAL_PREFIX,
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

    for tp in TranslatePage.objects.all():
        if rel_url == os.path.split(tp.file_name.name)[1]:
            # this also reinserts the prefix if it was missing...
            return our_static_pages_handler(request, tp.file_name.name)
    stat_path = os.path.join(settings.STATIC_URL, rel_url)
    return #redirect(stat_path)




def our_static_pages_handler(request, rel_url):
    lang_short = request.session.get(LANG_KEY,'en')
    new_lang = request.POST.get('lang') or request.GET.get('lang')
    if not (lang_short or new_lang):
        new_lang = 'en'

    lang_long_name = settings.LANGUAGES_DICT[lang_short]
    """    if new_lang:
        return set_lang_redirect(request, new_lang, rel_url)
    try:
        lang_long_name = settings.LANGUAGES_DICT[lang]
    except:
        return set_lang_redirect(request, 'en', rel_url)
    all_languages = settings.LANGUAGES
    europeana_item_count_mill = 22
    request_path = request.path
    reder
    """
    return render(request, rel_url, locals())


def set_lang_redirect(request, lang, next_page='/'):
    request.session[LANG_KEY] = lang
    if next_page[:6] != 'portal':
        next_page = os.path.join('/portal',next_page)
    return HttpResponseRedirect(next_page)
