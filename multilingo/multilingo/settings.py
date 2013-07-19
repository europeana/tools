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


# Django settings for europeana project.

import os

from django.core import exceptions

from local_settings import *



# Checking that all local settings exist and seems sane

try:
    DEBUG
except:
    DEBUG = False
    print 'Using default value for DEBUG =', DEBUG


try:
    DATABASES
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: DATABASES - see local_settings_template.py')


try:
    SECRET_KEY
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: SECRET_KEY - see local_settings_template.py')

try:
    DELIVER_STATIC_MEDIA
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: DELIVER_STATIC_MEDIA - see local_settings_template.py')

try:
    MEDIA_ROOT
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: MEDIA_ROOT - see local_settings_template.py')
if not os.path.exists(MEDIA_ROOT):
    raise exceptions.ImproperlyConfigured(
        'MEDIA_ROOT does not point to an existing directory - see local_settings_template.py')


try:
    MEDIA_FILE_PATH
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: MEDIA_FILE_PATH - see local_settings_template.py')
MEDIA_FILE_FULL_PATH = os.path.join(MEDIA_ROOT, MEDIA_FILE_PATH)
if not os.path.exists(MEDIA_FILE_FULL_PATH):
    raise exceptions.ImproperlyConfigured(
        'MEDIA_FILE_PATH does not point to an existing directory (%s) - see local_settings_template.py' % MEDIA_FILE_FULL_PATH)
for d in ('img','js','css'):
    media_dir = os.path.join(MEDIA_FILE_FULL_PATH, d)
    if not os.path.exists(media_dir):
        try:
            os.mkdir(media_dir)
        except:
            raise exceptions.ImproperlyConfigured(
                'Seems this process doesnt have write privs to %s' % media_dir)


try:
    TRANSLATIONS_ALLWAYS_SHOW_PROGRESS
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: TRANSLATIONS_ALLWAYS_SHOW_PROGRESS - see local_settings_template.py')
    
try:
    SUBMIT_PATH
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: SUBMIT_PATH - see local_settings_template.py')
if not os.path.exists(SUBMIT_PATH):
    raise exceptions.ImproperlyConfigured(
        'SUBMIT_PATH - must point to existing dir [%s]' % SUBMIT_PATH)
try:
    __tst_dir = os.path.join(SUBMIT_PATH, 'foo123')
    os.mkdir(__tst_dir)
except:
    raise exceptions.ImproperlyConfigured(
        'Seems this process doesnt have write privs to %s' % SUBMIT_PATH)
os.rmdir(__tst_dir)
del __tst_dir



try:
    SUBMIT_IS_SVN
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: SUBMIT_IS_SVN - see local_settings_template.py')



try:
    LANGUAGES
except:
    raise exceptions.ImproperlyConfigured(
        'Missing setting in local_settings.py: LANGUAGES - see local_settings_template.py')

try:
    i = 0
    for lang_code, label in LANGUAGES:
        i += 1
        if not isinstance(lang_code, basestring):
            raise
        if len(lang_code) < 2:
            raise
        if not isinstance(label, basestring):
            raise
except:
    raise exceptions.ImproperlyConfigured(
        'item no (%i) in LANGUAGES seems invalid - see local_settings_template.py' % i)




if proj_root[-1] == '/':
    # to make it consistent, remove trailing space
    proj_root = proj_root[:-1]

TEMPLATE_DEBUG = True

ADMINS = (
    #('Jacob Lundqvist', 'jacob.lundqvist@gmail.com'),
)

MANAGERS = ADMINS

#local_settings DATABASE_ENGINE = ''           # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
#local_settings DATABASE_NAME = ''             # Or path to database file if using sqlite3.
#local_settings DATABASE_USER = ''             # Not used with sqlite3.
#local_settings DATABASE_PASSWORD = ''         # Not used with sqlite3.
#local_settings DATABASE_HOST = ''             # Set to empty string for localhost. Not used with sqlite3.
#local_settings DATABASE_PORT = ''             # Set to empty string for default. Not used with sqlite3.

# Local time zone for this installation. Choices can be found here:
# http://en.wikipedia.org/wiki/List_of_tz_zones_by_name
# although not all choices may be available on all operating systems.
# If running in a Windows environment this must be set to the same as your
# system time zone.

TIME_ZONE = 'Europe/Amsterdam'

# Language code for this installation. All choices can be found here:
# http://www.i18nguy.com/unicode/language-identifiers.html
LANGUAGE_CODE = 'en-us'

SITE_ID = 1

# If you set this to False, Django will make some optimizations so as not
# to load the internationalization machinery.
USE_I18N = True

# If you set this to False, Django will not format dates, numbers and
# calendars according to the current locale
USE_L10N = True

# Absolute path to the directory that holds media.
# Example: "/home/media/media.lawrence.com/"
#local_settings MEDIA_ROOT = '/full/path/to/static/media'

# URL that handles the media served from MEDIA_ROOT. Make sure to use a
# trailing slash if there is a path component (optional in other cases).
# Examples: "http://media.lawrence.com", "http://example.com/media/"
MEDIA_URL = '/static_media/' #'/portal_static/'




# URL prefix for admin media -- CSS, JavaScript and images. Make sure to use a
# trailing slash.
# Examples: "http://foo.com/media/", "/media/".
ADMIN_MEDIA_PREFIX = '/admin_media/'

# Make this unique, and don't share it with anybody.
#local_settings SECRET_KEY = '3h&^gpvh*pn)r$$!)7g+8s^4!4jp6k17@#3gihk+vr8i4zty_h'



MIDDLEWARE_CLASSES = (
    'multilingo.gen_utils.reset_translation.ResetTranslationMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.locale.LocaleMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
)

ROOT_URLCONF = 'multilingo.urls'

TEMPLATE_DIRS = (
    # Put strings here, like "/home/html/django_templates" or "C:/www/django/templates".
    # Always use forward slashes, even on Windows.
    # Don't forget to use absolute paths, not relative paths.
    '%s/templates' % proj_root,
)

INSTALLED_APPS = (
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'django.contrib.messages',

    'django.contrib.admin',
    'django.contrib.admindocs',
    #'django.contrib.markup',

    'multilingo.rosetta',
    'multilingo.apps.multi_lingo',
)



PORTAL_PREFIX = 'portal'


LOCALE_PATHS = (
    "%s/apps/multi_lingo" % proj_root,
)

#
#=====================   Europeana languages settings   =======================
#

# just the lang keys for quick lookups
LANGUAGES_DICT = {}
for _k, _lbl in LANGUAGES:
    LANGUAGES_DICT[_k] = _lbl
# clean up of temp vars
del _k, _lbl




#
#======================   Rosetta specifics   =================================
#

# Number of messages to display per page.
ROSETTA_MESSAGES_PER_PAGE = 10

# Enable Google translation suggestions
ROSETTA_ENABLE_TRANSLATION_SUGGESTIONS = False

# Displays this language beside the original MSGID in the admin
#ROSETTA_MAIN_LANGUAGE = 'en'

# Change these if the source language in your PO files isn't English
#ROSETTA_MESSAGES_SOURCE_LANGUAGE_CODE = 'en'
#ROSETTA_MESSAGES_SOURCE_LANGUAGE_NAME = 'English'

"""
When running WSGI daemon mode, using mod_wsgi 2.0c5 or later, this setting
controls whether the contents of the gettext catalog files should be
automatically reloaded by the WSGI processes each time they are modified.

Notes:

 * The WSGI daemon process must have write permissions on the WSGI script file
   (as defined by the WSGIScriptAlias directive.)
 * WSGIScriptReloading must be set to On (it is by default)
 * For performance reasons, this setting should be disabled in production environments
 * When a common rosetta installation is shared among different Django projects,
   each one running in its own distinct WSGI virtual host, you can activate
   auto-reloading in individual projects by enabling this setting in the project's
   own configuration file, i.e. in the project's settings.py

Refs:

 * http://code.google.com/p/modwsgi/wiki/ReloadingSourceCode
 * http://code.google.com/p/modwsgi/wiki/ConfigurationDirectives#WSGIReloadMechanism

"""
ROSETTA_WSGI_AUTO_RELOAD = False
ROSETTA_UWSGI_AUTO_RELOAD = False


# Exclude applications defined in this list from being translated
ROSETTA_EXCLUDED_APPLICATIONS = (
    'django.contrib.admindocs',
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.sites',
    'django.contrib.messages',
    'django.contrib.sessions',
    'django.contrib.contenttypes',
    'multilingo.rosetta',
)

ROSETTA_USE_FUZZY = False
