import os

NODE_NAME = 'carola' # each node needs unique name in a thumbler swarm

ADMIN_EMAILS = ['jacob.lundqvist@europeana.eu',]

#
#  Debuging related
#
DEBUG = True

if not ('WINGDB_ACTIVE' in os.environ.keys()): 
    PLUGIN_FILTER = [

        #--- sipmanager
        'Watchdog',
        
        #--- dataset
        'DataSet',
        
        #--- syncmanager
        'SyncLeftovers', 
        'SyncManager',
        
        #---   plug_uris
        'UriPepareStorageDirs',
        #'UriValidateSave',
        ###'UriHandleBacklog',
        
        #--- statistics
        #'UpdateRequestStats',        
     ]
else:
    PLUGIN_FILTER = ['UriValidateSave',]





     
#
# Performance tweaking
#
MAX_LOAD_NEW_TASKS = (6.5, 5.8, 5.5)
MAX_LOAD_RUNNING_TASKS =  (15, 9, 8)
BACKLOG_AGE_MAX = 1 #3600 * 24 *   90  # last is number of days, only change this!
DB_RESET_INTERVAL = 0
THREAD_LIMIT = 7  # max allowed no of threads


#
#   DB access
#

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql_psycopg2', # Add 'postgresql_psycopg2', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': 'thumblr2',                      # Or path to database file if using sqlite3.
        # The following settings are not used with sqlite3:
        'USER': 'sipmanager',
        'PASSWORD': 'XXXXXX',
        #'HOST': '',                      # Empty for localhost through domain sockets or '127.0.0.1' for localhost through TCP.
        #'PORT': '',                      # Set to empty string for default.
    }
}


#
#  Various paths and file locations
#
FILES_PREFIX = '/dsk/large1/thumbler2_data/prod' # where all output is stored 
#FILES_PREFIX = '/dsk/hot_copy_large/thumbler2_data/prod' # where all output is stored 
TEMP_DIR = '%s/tmp' % FILES_PREFIX
MEDIA_ROOT = '%s/static-media' % FILES_PREFIX
SIP_OBJ_FILES = '%s/objs' % FILES_PREFIX
TIMESTAMP_FILES = '%s/timestamps' % FILES_PREFIX




if 'WINGDB_ACTIVE' in os.environ.keys():
    print '==== Assuming IDE mode  ===='
    THREADING_PLUGINS = False
    PRINT_LOG = True
    PROCESS_SLEEP_TIME = 5
    SIPMANAGER_DBG_LVL = 9
    TASK_PROGRESS_INTERVALL = 10
    SIP_LOG_FILE = '%s/logs-dbg/sipmanager.log' % FILES_PREFIX
else: # normal/standalone
    THREADING_PLUGINS = True # is threading allowed?
    PRINT_LOG = False # should log be printed to stdout in addition to logfile
    PROCESS_SLEEP_TIME = 30
    SIPMANAGER_DBG_LVL = 7
    TASK_PROGRESS_INTERVALL = 30 # how often the monitor display is updated
    SIP_LOG_FILE = '%s/logs/sipmanager.log' % FILES_PREFIX


#
#   Image Syncing
#
SYNC_SOURCE = SIP_OBJ_FILES
SYNC_DEST =  ['thumbler@5.22.148.146:/data/thumbler', # img1
              #'europeana@ingestion.europeana.eu:/repository/ingestion/img_cache/thumbler',
              #'thumbler@jumphost.eanadev.org:/mnt/thumbler',
              ]
IMG_SYNC_TOUCHFILE='/tmp/thumbler-sync-done'
SYNC_THINGS = (
    'BRIEF_DOC',
    'FULL_DOC',
)



# =====   Only for statistics plugin   ===
STATS_UPDATE_INTERVALL = 1800

# =====   only used by plugin webserver   ===
# Make this unique, and don't share it with anybody.
SECRET_KEY = '9843hnmdfmdcvy8934kjn34m,dfj9034ol.kjm'

