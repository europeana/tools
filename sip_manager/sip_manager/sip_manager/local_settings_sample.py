#
#=========================   Fix path   ====================================
#
import os.path

#Yes the extra normpath _is_ needed, otherwise proj_root becomes invalid
#when settings are called from apps...
proj_root = os.path.normpath(os.path.dirname(__file__))



DATABASE_ENGINE = 'postgresql_psycopg2'    # 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
DATABASE_NAME = 'sipmanager'             # Or path to database file if using sqlite3.
DATABASE_USER = 'sipmanager'             # Not used with sqlite3.
DATABASE_PASSWORD = 'replace with a password'         # Not used with sqlite3.
DATABASE_HOST = ''             # Set to empty string for localhost. Not used with sqlite3.
DATABASE_PORT = ''             # Set to empty string for default. Not used with sqlite3.



SECRET_KEY = '%yh05 long and random string uxv6)%'



# How old thumbnails are accepted when reprocessing collections
BACKLOG_AGE_MAX = 3600 * 24 * 90


# All output that is printed to stdout is also logged to this file
SIP_LOG_FILE = '/tmp/sip-manager.log'


# Where all downloaded Europeana:objects and generated images are stored
SIP_OBJ_FILES = '/Volumes/JacBook/SIP_object_files'


# In a multinode setup this name is displayed in the system monitor
# can be empty, but must be set
NODE_NAME = 'myserver'






#
# Should be a tupple/list of recievers for admin email - empty is fine
#
ADMIN_EMAILS = ('first@admin.domain', 'second@admin.domain')

#==============================================================================
#
# DummyIngestion settings
#  Until integration with Repox I use the module dummy_ingester this module
#  is pointed to a path, where it reads and parses all xml files found.










#==============================================================================
#
# Optional settings - What is given here is the default, if you dont plan
#                     to change it, not needed to included in your local_settings.py
#                     If a given optional is not specified, the default will be used
#                     and printed out

# If we allow processor to run in multithreaded
# If set to False, all plugins are run sequentaly in a single-threded way
# if not set the default will be printed and used
# THREADING_PLUGINS = False/True




# how often plugins should report what is happening (seconds)
# if not set the default will be printed and used
#TASK_PROGRESS_INTERVALL =


# How often we check for new tasks (seconds)
# if not set the default will be printed and used
#PROCESS_SLEEP_TIME =


# Limits amount of logging output (1-9)
# i normaly use 7 any higher value will be extreamly talkative
# if not set the default will be printed and used
#SIPMANAGER_DBG_LVL =

# If all log entries also should be printed to stdout
#PRINT_LOG = 



# If system load is over this, new tasks wont be started
# either a single float, or (prefered three values for 1, 5 and 15 min load)
# if not set the default will be printed and used
#MAX_LOAD_NEW_TASKS =

# If system load is over this, tasks will be terminated
# either a single float, or (prefered three values for 1, 5 and 15 min load)
# if not set the default will be printed and used
#MAX_LOAD_RUNNING_TASKS =




# This intervall in seconds indicates how often all plugins should be stopped
# in order for the databas connections to be closed
#DB_RESET_INTERVAL = 300

# If set and non zero this is max numbers of plugins that can be running
#THREAD_LIMIT = 40

#==============================================================================
#
# Debug related settings - should never be used under normal operation!
#

# This controlls if django should be run in debug mode, it gives more detailed
# error mesages when testing webpages...
# Important you really shouldnt enable this unless you know what you are
# doing, the dummyingester will eat gigantous amounts of memory...
DEBUG = False



# If given only the named plugins will be run
#  init plugins are excluded from this check - they are always run!
#  if not set the default will be printed and used
#PLUGIN_FILTER = ['UriCreate',]


#
# Indicate if sync reciever can handle scp 
#
ALLOW_SCP_SYNC = False
