import os
import sys

#
# 1) Rename to django.wsgi
# 2) replace with abs path to parrent dir
#

# the project dir for this django
sys.path.append('/var/local/proj/translomatic')
sys.path.append('/var/local/proj/translomatic/translomatic')


os.environ['DJANGO_SETTINGS_MODULE'] = 'settings.py'

import django.core.handlers.wsgi
application = django.core.handlers.wsgi.WSGIHandler()
