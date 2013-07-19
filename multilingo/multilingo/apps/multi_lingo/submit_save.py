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


DJANGO_SITENAME=multilingo
DJANGO_SETTINGS_MODULE=${DJANGO_SITENAME}.settings
"""

import codecs
import os
import shutil
import time

from django.conf import settings
from django.core import exceptions
from django.template import loader, Context, Template
from django.utils import translation

from multilingo.gen_utils.shell_cmd import cmd_execute
from gen_utils.submit_base import DIR_STATIC_PAGES, DIR_MSG_KEYS, DIR_MSG_KEYS2, DIR_LOCALES, SYNC_INDICATOR, SubmitBase

from utils import global_environ, SubmitError


import models




THIS_DIR = os.path.split(__file__)[0]


DIR_EXTRA_MEDIA = os.path.join(DIR_STATIC_PAGES, settings.MEDIA_FILE_PATH)

PROP_FILE_FNAME_BASE = 'prop_file'
FNAME_MSG_KEYS = '%s.html' % PROP_FILE_FNAME_BASE
FNAME_MSG_KEYS2 = '%s2.html' % PROP_FILE_FNAME_BASE

SVN_COMMIT_INITIATED_BY = '' # who started the svn commit process



if settings.SUBMIT_IS_SVN:
    for d in (DIR_STATIC_PAGES, DIR_MSG_KEYS):
        d_svn = os.path.join(d, '.svn')
        if not os.path.exists(d_svn):
            raise exceptions.ImproperlyConfigured(
                '%s not prepared for subversion' % d_svn)


htmlcodes = ['&Aacute;', '&aacute;', '&Agrave;', '&Acirc;', '&agrave;', '&Acirc;', '&acirc;', '&Auml;', '&auml;',
             '&Atilde;', '&atilde;', '&Aring;', '&aring;', '&Aelig;', '&aelig;', '&Ccedil;', '&ccedil;', '&Eth;',
             '&eth;', '&Eacute;', '&eacute;', '&Egrave;', '&egrave;', '&Ecirc;', '&ecirc;', '&Euml;', '&euml;',
             '&Iacute;', '&iacute;', '&Igrave;', '&igrave;', '&Icirc;', '&icirc;', '&Iuml;', '&iuml;', '&Ntilde;',
             '&ntilde;', '&Oacute;', '&oacute;', '&Ograve;', '&ograve;', '&Ocirc;', '&ocirc;', '&Ouml;', '&ouml;',
             '&Otilde;', '&otilde;', '&Oslash;', '&oslash;', '&szlig;', '&Thorn;', '&thorn;', '&Uacute;',
             '&uacute;', '&Ugrave;', '&ugrave;', '&Ucirc;', '&ucirc;', '&Uuml;', '&uuml;', '&Yacute;', '&yacute;',
             '&yuml;', '&copy;', '&reg;', '&trade;', '&euro;', '&cent;', '&pound;', '&lsquo;', '&rsquo;', '&ldquo;',
             '&rdquo;', '&laquo;', '&raquo;', '&mdash;', '&ndash;', '&deg;', '&plusmn;', '&frac14;', '&frac12;',
             '&frac34;', '&times;', '&divide;', '&alpha;', '&beta;', '&infin']
funnychars = ['\xc1','\xe1','\xc0','\xc2','\xe0','\xc2','\xe2','\xc4','\xe4','\xc3','\xe3','\xc5','\xe5','\xc6',
              '\xe6','\xc7','\xe7','\xd0','\xf0','\xc9','\xe9','\xc8','\xe8','\xca','\xea','\xcb','\xeb','\xcd',
              '\xed','\xcc','\xec','\xce','\xee','\xcf','\xef','\xd1','\xf1','\xd3','\xf3','\xd2','\xf2','\xd4',
              '\xf4','\xd6','\xf6','\xd5','\xf5','\xd8','\xf8','\xdf','\xde','\xfe','\xda','\xfa','\xd9','\xf9',
              '\xdb','\xfb','\xdc','\xfc','\xdd','\xfd','\xff','\xa9','\xae','\u2122','\u20ac','\xa2','\xa3',
              '\u2018','\u2019','\u201c','\u201d','\xab','\xbb','\u2014','\u2013','\xb0','\xb1','\xbc','\xbd',
              '\xbe','\xd7','\xf7','\u03b1','\u03b2','\u221e']



       

class SubmitSaver(SubmitBase):

    def __init__(self, request):

        self.request = request
        self.b_static_pages_saved = False # will be set if any static pages are saved
        self.b_support_media_saved = False

        # Remember the normal template path so we can revert back to it
        self.org_template_dirs = settings.TEMPLATE_DIRS
        
        # double check were not about to erase the system
        if settings.SUBMIT_PATH in ('', '/', '/etc','/usr','/var'):
            raise exceptions.ImproperlyConfigured(
                'SUBMIT_PATH = %s not allowed' % settings.SUBMIT_PATH)


    def run(self):
        global SVN_COMMIT_INITIATED_BY
        if not self.request.user.email:
            raise exceptions.ImproperlyConfigured(
                'No email configured for current user!')
        
        if SVN_COMMIT_INITIATED_BY:
            # somebody is doing a save
            raise exceptions.PermissionDenied(
                'Somebody already started a submit: %s!' % SVN_COMMIT_INITIATED_BY)
        if os.path.exists(SYNC_INDICATOR):
            # somebody has already triggered a submit
            raise exceptions.PermissionDenied(
                'A submit is already done, waiting for propagation to subversion, try again in a few minutes')

        self.log('run(%s)' % self.request.user)
        SVN_COMMIT_INITIATED_BY = self.request.user
        try:
            self.handle_all_static_pages()
            self.handle_support_media()
            self.handle_loacle_files()
        #except SubmitError as e:
            
        except:
            SVN_COMMIT_INITIATED_BY = ''
            raise
        open(SYNC_INDICATOR,'a').write('%s\n' % self.request.user.email)
        self.log('preparing pages for commit done')
        SVN_COMMIT_INITIATED_BY = ''

    def handle_all_static_pages(self):
        self.log('handle_all_static_pages()')
        self.add_webcommitter(DIR_STATIC_PAGES)
        self.add_webcommitter(DIR_MSG_KEYS)
        
        static_pages = models.TranslatePage.objects.filter(active=True)
        for static_page in static_pages:
            self.static_page_handle('', static_page)
            
        rights_pages = models.RightsPage.objects.filter(active=True)
        for rights_page in rights_pages:
            self.static_page_handle('rights', rights_page)
            
    def handle_support_media(self):
        self.log('handle_support_media()')
        self.save_media_group(models.MediaIMG.objects.all(), 'img')
        self.save_media_group(models.MediaCSS.objects.all(), 'css')
        self.save_media_group(models.MediaJS.objects.all(), 'js')

        

    def handle_loacle_files(self):
        self.add_webcommitter(DIR_LOCALES)
        for dirpath, dirnames, filenames in os.walk(os.path.join(THIS_DIR, 'locale')):
            if dirpath.find('.svn') > -1:
                continue # skip svn trees
            try:                
                p = dirpath.split('locale/')[1]
                destpath = os.path.join(DIR_LOCALES, p)
            except:
                continue # we where in locale top dir
            for filename in filenames:
                if filename[-3:] == '.po':
                    if not os.path.exists(destpath):
                        os.makedirs(destpath)
                    shutil.copy2(os.path.join(dirpath, filename), os.path.join(destpath, filename))
            
        
    def static_page_handle(self, prefix, static_page):
        self.log('static_page_handle(%s)' % static_page.file_name.name)
        
        template_fname = static_page.file_name.name
        for lang, lang_name in settings.LANGUAGES:
            html = self.translate_template(template_fname, lang)
            if template_fname.find('%s' % PROP_FILE_FNAME_BASE) < 0:
                self.static_page_save(prefix, template_fname, html, lang)
                self.b_static_pages_saved = True
            else:
                if template_fname.find(FNAME_MSG_KEYS2) < 0:
                    # do vers1 prop file
                    dest_dir = DIR_MSG_KEYS
                else:
                    # was vers2 prop file
                    dest_dir = DIR_MSG_KEYS2
                self.prop_page_save(dest_dir, template_fname, html, lang)
                    
                
    def static_page_save(self, prefix, template_fname, html, lang):
        """
          aboutus_content_en.html
          aboutus_header_en.html
          aboutus_left_en.html
          aboutus_title_en.html
        """
        self.log('static_page_save(%s - %s)' % (template_fname, lang))
        page_name = os.path.splitext(os.path.split(template_fname)[1])[0]
        if prefix:
            page_name = os.path.join(prefix, page_name)
        for key, part_name in ( ('static_title', 'title'),
                            ('static_header_scripts', 'header'),
                            ('static_content', 'content'),
                            ('static_left_side', 'left'),
                            ):
            try:
                partial_html = self.html_extract(key, html)
            except SubmitError as e:
                raise SubmitError('File %s is missing multilingo tag %s' % (template_fname, e.value))
                
            full_path = os.path.join(DIR_STATIC_PAGES, '%s_%s_%s.html' % (page_name, part_name, lang))
            self.save_content(full_path, partial_html)
        return

    def prop_page_save(self, dest_dir, template_fname, html, lang):
        self.log('prop_page_save(%s, %s)' % (dest_dir, lang))
        data = self.convert_html_to_prop(html)
        full_path = os.path.join(dest_dir, 'messages_%s.properties' % lang)
        self.save_content(full_path, data)
        
        
    def convert_html_to_prop(self, html):
        self.log('convert_html_to_prop()')
        in_lines = html.split('\n')
        out_lines = []
        for in_line in in_lines:
            if in_line.find(u'_t</td>') < 0:
                continue # not a tag line
            columns = in_line.split('</td><td>')
            if len(columns) < 2:
                continue
            try:
                #key = in_line.split('<tr><td>')[1].split('</td><td>')[0]
                #value = in_line.split('</td><td>')[1].split('</td></tr>')[0]
                key = columns[0].split('<td>')[1].strip()
                value = columns[1].split('</td>')[0].strip()
            except:
                raise exceptions.ImproperlyConfigured('Propfile lines needs to be in format: <tr><td>key</td><td>value</td></tr>\nOffending line:\n%s' % in_line)
            out_lines.append(u'%s=%s\n' % (key, value))
        return ''.join(out_lines)
        
    def translate_template(self, template_fname, lang):
        #self.log('translate_template(%s)' % lang)
        settings.TEMPLATE_DIRS = (os.path.join(THIS_DIR, 'templates_submit'),
                                  os.path.join(THIS_DIR, 'templates'))
        # Activate language
        translation.activate(lang)

        # render page
        # Retrive a template specifying a template directory to check
        t, name = loader.find_template(template_fname)
                                        #(
                                        # os.path.join(THIS_DIR, 'templates_submit'),
                                        # os.path.join(THIS_DIR, 'templates'),
                                        # ))
        if isinstance(t, unicode):
            # Boy do I hate this, sometimes its a template obj, sometimes
            # an unicode dump of the context - how consistent is that???
            t = Template(t)

        #tmpl, origin = loader.find_template(template_fname)#, self.templates_submit)
        c = Context()
        c.update(global_environ(None))
        html = t.render(c)
        # reset template path asap!
        settings.TEMPLATE_DIRS = self.org_template_dirs
        return html

    
    def save_media_group(self, items, dest_dir):
        self.log('save_media_group(%s)' % dest_dir)
        for item in items:
            itm_fname = item.file_name.name
            full_dest_dir = os.path.join(DIR_EXTRA_MEDIA, dest_dir)
            try:
                os.makedirs(full_dest_dir)
            except OSError:
                pass # existed
            shutil.copy(os.path.join(settings.MEDIA_ROOT, itm_fname), full_dest_dir)
            self.b_support_media_saved = True
        return

              
    #
    # Generics
    #
    def html_extract(self, key, html):
        #self.log('html_extract()')
        try:
            foo, content = html.split('<%s>' % key)
        except:
            raise SubmitError('%s' % key)
                
        relevant_part,foo = content.split('</%s>' % key)
        return relevant_part
    
    def save_content(self, file_name, content):
        #self.log('save_content()')
        codecs.open(file_name, 'w', 'utf-8').write(content)

    def add_webcommitter(self, ddir):
        open(os.path.join(ddir, 'webcommiters.log'), 
             'a').write('%s %s\n' % (time.asctime(), self.request.user.email))


        
        
