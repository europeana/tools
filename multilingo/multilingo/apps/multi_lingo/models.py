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
import time
import threading

from django.conf import settings
from django.core import urlresolvers
from django.db import models
from django.db.models.signals import post_save, pre_delete, post_delete

from django.core.files.storage import FileSystemStorage

from multilingo.gen_utils.shell_cmd import cmd_execute

sys.setrecursionlimit(1500)
THIS_DIR = os.path.normpath(os.path.dirname(__file__))
#os.path.split(__file__)[0]
TEMPLATES_DIR = os.path.join(THIS_DIR, 'templates')
STATIC_PAGES = 'static_pages'
RIGHTS_PAGES = 'rights'

T_UPD_TRANS = None # timer object for delayed translate updates on multiple add/delete operations
TIME_DELAY_TRANSLATIONS = 2.0


OUR_STATIC_PAGES = {}


class TranslatePage(models.Model):
    """
    A page that should be translated
    """
    file_name = models.FileField(upload_to=STATIC_PAGES, storage=FileSystemStorage(location=TEMPLATES_DIR))
    active = models.BooleanField('If checked this page will be included in submits to production',
                                 default=False)
    time_created = models.DateTimeField(auto_now_add=True,editable=False)

    class Meta:
        ordering = ['file_name']
        
    def __unicode__(self):
        return self.file_name.name

class RightsPage(models.Model):
    file_name = models.FileField(upload_to=RIGHTS_PAGES, storage=FileSystemStorage(location=TEMPLATES_DIR))
    active = models.BooleanField('If checked this page will be included in submits to production',
                                 default=False)
    time_created = models.DateTimeField(auto_now_add=True,editable=False)
    

class MediaIMG(models.Model):
    file_name = models.FileField(upload_to=os.path.join(settings.MEDIA_FILE_PATH, 'img'))
    class Meta:
        ordering = ['file_name']
        verbose_name_plural = 'media img'

    def __unicode__(self):
        return self.file_name.name

class MediaJS(models.Model):
    file_name = models.FileField(upload_to=os.path.join(settings.MEDIA_FILE_PATH, 'js'))
    class Meta:
        ordering = ['file_name']
        verbose_name_plural = 'media js'

    def __unicode__(self):
        return self.file_name.name

class MediaCSS(models.Model):
    file_name = models.FileField(upload_to=os.path.join(settings.MEDIA_FILE_PATH, 'css'))
    class Meta:
        ordering = ['file_name']
        verbose_name_plural = 'media css'

    def __unicode__(self):
        return self.file_name.name





class Template(models.Model):
    file_name = models.FileField(upload_to='templates', storage=FileSystemStorage(location=THIS_DIR))

    def __unicode__(self):
        return os.path.split(self.file_name.name)[1]






def file_cleanup_cb(sender, **kwargs):
    """
    File cleanup callback used to emulate the old delete
    behavior using signals. Initially django deleted linked
    files when an object containing a File/ImageField was deleted.

    Usage:

    >>> from django.db.models.signals import post_delete

    >>> post_delete.connect(file_cleanup, sender=MyModel, dispatch_uid="mymodel.file_cleanup")
    """
    for fieldname in sender._meta.get_all_field_names():
        try:
            field = sender._meta.get_field(fieldname)
        except:
            field = None
        if field and isinstance(field, models.FileField):
            inst = kwargs['instance']
            f = getattr(inst, fieldname)
            m = inst.__class__._default_manager
            if hasattr(f, 'path') and os.path.exists(f.path) \
                and not m.filter(**{'%s__exact' % fieldname: getattr(inst, fieldname)})\
                .exclude(pk=inst._get_pk_val()):
                    try:
                        #os.remove(f.path)
                        f.delete()
                    except:
                        pass
    




def update_translations(show_progess=False,recursion=0):
    if settings.TRANSLATIONS_ALLWAYS_SHOW_PROGRESS:
        show_progess = True
    b = do_update_translations(show_progess, recursion)
    if not b:
        if show_progess:
            print 'Due to error, re-running this'
        time.sleep(10)
        b = update_translations(show_progess, recursion+1)
    if show_progess:
        print 'completed, recursion=', recursion
    return b
        
def do_update_translations(show_progess=False,recursion=0):
    if show_progess:
        print 'running update_translations, recursion=', recursion
    if recursion > 5:
        if show_progess:
            print 'Max recursion depth exceeded (%i) aborting update_translations()' % recursion
            return True # indicate no recursion should happen
    update_template_list()

    if show_progess:
        print 'makemessages...'
    output = cmd_execute('python ../../manage.py makemessages -a',
                          cwd=THIS_DIR)
    if output:
        if show_progess:
            print output
        return False
        
    if show_progess:
        print 'compilemessages...'
    output2 = cmd_execute('python ../../manage.py compilemessages',
                          cwd=THIS_DIR)
    if output2:
        if show_progess:
            print output2
        return False

    a = urlresolvers.resolve('/')
    if show_progess:
        print 'Translating Done!'
    return True


def update_template_list():
    global OUR_STATIC_PAGES
    # project/app/locale

    OUR_STATIC_PAGES = {}
    for static_page in RightsPage.objects.all():
        fname = os.path.split(static_page.file_name.name)
        OUR_STATIC_PAGES[static_page.file_name.name] = static_page.file_name.name
    for static_page in TranslatePage.objects.all():
        fname = os.path.split(static_page.file_name.name)[1]
        OUR_STATIC_PAGES[fname] = static_page.file_name.name



def update_translations_cb(sender, instance, *args, **kwargs):
    """We use a timer to make sure we only run update_translations() once on
    multiple file operations"""
    global T_UPD_TRANS
    if T_UPD_TRANS and T_UPD_TRANS.isAlive():
        T_UPD_TRANS.cancel()
    T_UPD_TRANS = threading.Timer(TIME_DELAY_TRANSLATIONS, update_translations)
    T_UPD_TRANS.start()

post_save.connect(update_translations_cb, sender=TranslatePage)
pre_delete.connect(file_cleanup_cb, sender=TranslatePage, dispatch_uid="mymodel.file_cleanup")
post_delete.connect(update_translations_cb, sender=TranslatePage)

post_save.connect(update_translations_cb, sender=RightsPage)
pre_delete.connect(file_cleanup_cb, sender=RightsPage, dispatch_uid="mymodel.file_cleanup")
post_delete.connect(update_translations_cb, sender=RightsPage)

pre_delete.connect(file_cleanup_cb, sender=MediaIMG, dispatch_uid="mymodel.file_cleanup")
pre_delete.connect(file_cleanup_cb, sender=MediaJS, dispatch_uid="mymodel.file_cleanup")
pre_delete.connect(file_cleanup_cb, sender=MediaCSS, dispatch_uid="mymodel.file_cleanup")
pre_delete.connect(file_cleanup_cb, sender=Template, dispatch_uid="mymodel.file_cleanup")








#
#  Startup checks
#

if 'WINGDB_ACTIVE' not in os.environ:
    # Dont run this from within the debugger to save time during development
    if sys.argv[-1] == 'runserver':
        # make sure we are uptodate on translations...
        update_translations_cb(None,None)
