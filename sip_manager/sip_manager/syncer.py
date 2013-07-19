import os
import sys

try:
    import django.db
    django_found = True
except:
    django_found = False
if django_found:
    print '*** this limited runner should only be used if you dont have django installed!!'
    sys.exit(1)

import time
from django.conf import settings
from apps.plug_uris.syncmanager import SyncManager
from apps.sipmanager import sip_task

if not settings.IMG_SYNC_TOUCHFILE:
    print '*** settings.IMG_SYNC_TOUCHFILE not valid'
    sys.exit(1)
    
if settings.THREADING_PLUGINS:
    print '*** threading mode not allowed when stand alone syncing.'
    sys.exit(1)

sleep_time = 30
print 'Processing pending syncs every %i seconds' % sleep_time
print ' touching %s after each iteration as keep-alive indicator' % settings.IMG_SYNC_TOUCHFILE
sm = SyncManager(debug_lvl=settings.SIPMANAGER_DBG_LVL, 
                 touch_file=settings.IMG_SYNC_TOUCHFILE)
while True:
    ctrl_c = False
    file_count = len(sm.prepare())
    if file_count:
        try:
            print 'Will process %i sync files' % file_count
            sm.run()
        except KeyboardInterrupt:
            ctrl_c = True
    if ctrl_c:
        print 'SyncManager failed - terminating'
        sys.exit(1)
    if sip_task.PLUGINS_MAY_NOT_RUN or sip_task.IS_TERMINATING:
        print 'SyncManager is indicting terminate request, so being polite I quit!'
        sys.exit(1)
    sys.stdout.write('.')
    sys.stdout.flush()
    os.utime(settings.IMG_SYNC_TOUCHFILE, None)
    time.sleep(sleep_time)
    
    