# copy to local_settings.py

NODE_NAME = 'samplename' # each node needs unique name in a thumbler swarm

DEBUG = True
THREADING_PLUGINS = True # is threading allowed?

THREAD_LIMIT = 0  # max allowed no of threads
SIPMANAGER_DBG_LVL = 7

MAX_LOAD_NEW_TASKS = (3.2, 3.3, 3.4)
MAX_LOAD_RUNNING_TASKS =  (3.8, 3.9, 5.0)


DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql_psycopg2', # Add 'postgresql_psycopg2', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': 'thumblr2',                      # Or path to database file if using sqlite3.
        # The following settings are not used with sqlite3:
        'USER': '',
        'PASSWORD': '',
        'HOST': '',                      # Empty for localhost through domain sockets or '127.0.0.1' for localhost through TCP.
        'PORT': '',                      # Set to empty string for default.
    }
}


# Make this unique, and don't share it with anybody.
SECRET_KEY = '-043oj3lk43j890943u@#%$#%^$8987fdhjkdnbv t8795'



FILES_PREFIX = '/tmp/tmp/tmp' # where all output is stored 
SIP_OBJ_FILES = '%s/objs' % FILES_PREFIX
SIP_LOG_FILE = '%s/logs/sipmanager.log' % FILES_PREFIX
TIMESTAMP_FILES = '%s/timestamps' % FILES_PREFIX

SYNC_DEST = '/Users/jaclu/tmp/syncrecieve' # where to sync thumbnails
IMG_SYNC_TOUCHFILE='/tmp/thumbler-sync-done'

# Absolute filesystem path to the directory that will hold user-uploaded files.
# Example: "/var/www/example.com/media/"
MEDIA_ROOT = '/tmp/tmp/thumblr2-media'


PRINT_LOG = True # should log be printed to stdout in addition to logfile



TASK_PROGRESS_INTERVALL = 30 # how often the monitor display is updated
STATS_UPDATE_INTERVALL = 120
BACKLOG_AGE_MAX = 1 #3600 * 24 *   90  # last is number of days, only change this!
PROCESS_SLEEP_TIME = 10
DB_RESET_INTERVAL = 0


