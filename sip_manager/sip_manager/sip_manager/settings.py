# Django settings for thumblr project.

import os.path

from django.core.exceptions import ImproperlyConfigured

#Yes the extra normpath _is_ needed, otherwise proj_root becomes invalid
#when settings are called from apps...
proj_root = os.path.normpath(os.path.dirname(__file__))


from local_settings import *


# Verify we have all local_settings

try:
    MEDIA_ROOT
except:
    raise ImproperlyConfigured('Missing setting MEDIA_ROOT - see sample_local_settings.py')

if not os.path.exists(MEDIA_ROOT):
    raise ImproperlyConfigured('MEDIA_ROOT must point to an existing dir - see sample_local_settings.py')






try:
    DATABASES
except:
    # old style single database
    try:
        DATABASE_ENGINE
    except:
        raise ImproperlyConfigured('Missing setting DATABASE_ENGINE - see local_settings_sample.py')


    try:
        DATABASE_NAME
    except:
        raise ImproperlyConfigured('Missing setting DATABASE_NAME - see local_settings_sample.py')


    try:
        DATABASE_USER
    except:
        raise ImproperlyConfigured('Missing setting DATABASE_USER - see local_settings_sample.py')


    try:
        DATABASE_PASSWORD
    except:
        raise ImproperlyConfigured('Missing setting DATABASE_PASSWORD - see local_settings_sample.py')


    try:
        DATABASE_HOST
    except:
        raise ImproperlyConfigured('Missing setting DATABASE_HOST - see local_settings_sample.py')


    try:
        DATABASE_PORT
    except:
        raise ImproperlyConfigured('Missing setting DATABASE_PORT - see local_settings_sample.py')


try:
    SECRET_KEY
except:
    raise ImproperlyConfigured('Missing setting SECRET_KEY - see local_settings_sample.py')


try:
    SIP_LOG_FILE
except:
    raise ImproperlyConfigured('Missing setting SIP_LOG_FILE - see local_settings_sample.py')


try:
    SIP_OBJ_FILES
    if SIP_OBJ_FILES[-1] == '/':
        raise
except:
    raise ImproperlyConfigured('Missing setting SIP_OBJ_FILES - see local_settings_sample.py')



#
# DummyIngestion settings
#  Until integration with Repox I use the module dummy_ingester this module
#  is pointed to a path, where it reads and parses all xml files found.
#
try:
    IMPORT_SCAN_TREE
    b = True
except:
    b = False
if b:
    raise ImproperlyConfigured('IMPORT_SCAN_TREE - obsolete remove from local_settings.py')

try:
    TREE_IS_INGESTION_SVN
    b = True
except:
    b = False
if b:
    raise ImproperlyConfigured('TREE_IS_INGESTION_SVN - obsolete remove from local_settings.py')

try:
    PATH_COLLECTIONS
except:
    PATH_COLLECTIONS = None


try:
    TIMESTAMP_FILES
except:
    raise ImproperlyConfigured('Missing setting TIMESTAMP_FILES - see local_settings_sample.py')

if not os.path.exists(TIMESTAMP_FILES):
    try:
        os.mkdir(TIMESTAMP_FILES)
    except:
        print TIMESTAMP_FILES
        raise ImproperlyConfigured('TIMESTAMP_FILES Failed to create directory')
    

try:
    STATS_UPDATE_INTERVALL
except:
    raise ImproperlyConfigured('Missing setting STATS_UPDATE_INTERVALL - see local_settings_sample.py')
        
#
#  Optional settings, if not given default is used
#
try:
    THREADING_PLUGINS
except:
    THREADING_PLUGINS = True
    print 'Using default value for THREADING_PLUGINS =', THREADING_PLUGINS


try:
    TASK_PROGRESS_INTERVALL
except:
    TASK_PROGRESS_INTERVALL = 15
    print 'Using default value for TASK_PROGRESS_INTERVALL =', TASK_PROGRESS_INTERVALL

try:
    PROCESS_SLEEP_TIME
except:
    PROCESS_SLEEP_TIME = 60
    print 'Using default value for PROCESS_SLEEP_TIME =', PROCESS_SLEEP_TIME


try:
    SIPMANAGER_DBG_LVL
except:
    SIPMANAGER_DBG_LVL = 7
    print 'Using default value for SIPMANAGER_DBG_LVL =', SIPMANAGER_DBG_LVL

try:
    PRINT_LOG
except:
    PRINT_LOG = True
    print 'Using default value for PRINT_LOG =', PRINT_LOG

try:
    MAX_LOAD_NEW_TASKS
except:
    MAX_LOAD_NEW_TASKS = (1.7, 1.8,  1.9)
    print 'Using default value for MAX_LOAD_NEW_TASKS = (%0.1f, %0.1f, %0.1f)' % MAX_LOAD_NEW_TASKS
try:
    float(MAX_LOAD_NEW_TASKS)
    MAX_LOAD_NEW_TASKS = (MAX_LOAD_NEW_TASKS,
                          MAX_LOAD_NEW_TASKS,
                          MAX_LOAD_NEW_TASKS)
except:
    try:
        a,b,c = MAX_LOAD_NEW_TASKS
        float(a)
        float(b)
        float(c)
    except:
        raise ImproperlyConfigured('MAX_LOAD_NEW_TASKS must be a float or a tupple of three floats - see local_settings_sample.py')


try:
    MAX_LOAD_RUNNING_TASKS
except:
    MAX_LOAD_RUNNING_TASKS = (3.0, 3.2, 4.0)
    print 'Using default value for MAX_LOAD_RUNNING_TASKS = (%0.1f, %0.1f, %0.1f)' % MAX_LOAD_RUNNING_TASKS
try:
    float(MAX_LOAD_RUNNING_TASKS)
    MAX_LOAD_RUNNING_TASKS = (MAX_LOAD_RUNNING_TASKS,
                              MAX_LOAD_RUNNING_TASKS,
                              MAX_LOAD_RUNNING_TASKS)
except:
    try:
        a,b,c = MAX_LOAD_RUNNING_TASKS
        float(a)
        float(b)
        float(c)
    except:
        raise ImproperlyConfigured('MAX_LOAD_RUNNING_TASKS must be a float or a tupple of three floats - see local_settings_sample.py')


try:
    DB_RESET_INTERVAL    
except:
    raise ImproperlyConfigured('Missing setting DB_RESET_INTERVAL - see local_settings_sample.py')
    
try:
    THREAD_LIMIT
except:
    raise ImproperlyConfigured('Missing setting THREAD_LIMIT - see local_settings_sample.py')

try:
    ADMIN_EMAILS
    if not isinstance(ADMIN_EMAILS, (list, tuple)):
        raise
except:
    raise ImproperlyConfigured('Missing setting ADMIN_EMAILS - see local_settings_sample.py')

try:
    SYNC_THINGS
except:
    SYNC_THINGS = None
try:
    SYNC_DEST
except:
    SYNC_DEST = None
try:
    IMG_SYNC_TOUCHFILE
except:
    IMG_SYNC_TOUCHFILE = None


try:
    ALLOW_SCP_SYNC
except:
    raise ImproperlyConfigured('Missing setting ALLOW_SCP_SYNC - see local_settings_sample.py')

try:
    int(BACKLOG_AGE_MAX)
except:
    BACKLOG_AGE_MAX = 3600 * 24 * 90
    print 'Using default value for BACKLOG_AGE_MAX =', BACKLOG_AGE_MAX


try:
    if SYNC_DEST[-1] == '/':
        raise
except:
    raise ImproperlyConfigured('SYNC_DEST Cant end with a "/"')
    
#
#   Debug settings
#
try:
    DEBUG
except:
    DEBUG = False
    print 'Using default value for DEBUG =', DEBUG

try:
    PLUGIN_FILTER
except:
    PLUGIN_FILTER = []









# ====== generic settings, not site dependant

TEMPLATE_DEBUG = DEBUG

ADMINS = (
    # ('Your Name', 'your_email@example.com'),
)

MANAGERS = ADMINS


# Hosts/domain names that are valid for this site; required if DEBUG is False
# See https://docs.djangoproject.com/en/1.5/ref/settings/#allowed-hosts
ALLOWED_HOSTS = []

# Local time zone for this installation. Choices can be found here:
# http://en.wikipedia.org/wiki/List_of_tz_zones_by_name
# although not all choices may be available on all operating systems.
# In a Windows environment this must be set to your system time zone.
TIME_ZONE = 'Europe/Amsterdam'

# Language code for this installation. All choices can be found here:
# http://www.i18nguy.com/unicode/language-identifiers.html
LANGUAGE_CODE = 'en-us'

SITE_ID = 1

# If you set this to False, Django will make some optimizations so as not
# to load the internationalization machinery.
USE_I18N = True

# If you set this to False, Django will not format dates, numbers and
# calendars according to the current locale.
USE_L10N = True

# If you set this to False, Django will not use timezone-aware datetimes.
USE_TZ = True


# Absolute path to the directory that holds media.
# Example: "/home/media/media.lawrence.com/"
MEDIA_ROOT = '%s/media' % proj_root

# URL that handles the media served from MEDIA_ROOT. Make sure to use a
# trailing slash.
# Examples: "http://example.com/media/", "http://media.example.com/"
MEDIA_URL = '/static_media/'

# Absolute path to the directory static files should be collected to.
# Don't put anything in this directory yourself; store your static files
# in apps' "static/" subdirectories and in STATICFILES_DIRS.
# Example: "/var/www/example.com/static/"
STATIC_ROOT = ''

# URL prefix for static files.
# Example: "http://example.com/static/", "http://static.example.com/"
STATIC_URL = '/static/'

# Additional locations of static files
STATICFILES_DIRS = (
    # Put strings here, like "/home/html/static" or "C:/www/django/static".
    # Always use forward slashes, even on Windows.
    # Don't forget to use absolute paths, not relative paths.
)

# List of finder classes that know how to find static files in
# various locations.
STATICFILES_FINDERS = (
    'django.contrib.staticfiles.finders.FileSystemFinder',
    'django.contrib.staticfiles.finders.AppDirectoriesFinder',
#    'django.contrib.staticfiles.finders.DefaultStorageFinder',
)

# List of callables that know how to import templates from various sources.
TEMPLATE_LOADERS = (
    'django.template.loaders.filesystem.Loader',
    'django.template.loaders.app_directories.Loader',
#     'django.template.loaders.eggs.Loader',
)

MIDDLEWARE_CLASSES = (
    'django.middleware.common.CommonMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    # Uncomment the next line for simple clickjacking protection:
    # 'django.middleware.clickjacking.XFrameOptionsMiddleware',
)

ROOT_URLCONF = 'urls'

## Python dotted path to the WSGI application used by Django's runserver.
#WSGI_APPLICATION = 'thumblr.wsgi.application'

TEMPLATE_DIRS = (
    # Put strings here, like "/home/html/django_templates" or "C:/www/django/templates".
    # Always use forward slashes, even on Windows.
    # Don't forget to use absolute paths, not relative paths.
    '%s/templates' % proj_root,
    
    # seriously doubt it is used anymore
    #'/Library/Python/2.6/site-packages/djblets/datagrid/templates',
)

INSTALLED_APPS = (
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.sites',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    # Uncomment the next line to enable the admin:
    'django.contrib.admin',
    # Uncomment the next line to enable admin documentation:
    'django.contrib.admindocs',
    
    # base sipmanager modules
    'apps.sipmanager',
    'apps.dummy_ingester',
    'apps.base_item',
    
    # plugins
    'apps.plug_uris',
    
    'apps.optout',
    
    #'apps.ingqueue',
    
    'apps.log',
    'apps.statistics',    
)

# A sample logging configuration. The only tangible logging
# performed by this configuration is to send an email to
# the site admins on every HTTP 500 error when DEBUG=False.
# See http://docs.djangoproject.com/en/dev/topics/logging for
# more details on how to customize your logging configuration.
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'filters': {
        'require_debug_false': {
            '()': 'django.utils.log.RequireDebugFalse'
        }
    },
    'handlers': {
        'mail_admins': {
            'level': 'ERROR',
            'filters': ['require_debug_false'],
            'class': 'django.utils.log.AdminEmailHandler'
        }
    },
    'loggers': {
        'django.request': {
            'handlers': ['mail_admins'],
            'level': 'ERROR',
            'propagate': True,
        },
    }
}


DAJAXICE_FUNCTIONS = (
	'examples.ajax.randomize',
)
