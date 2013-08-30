

import time
from datetime import datetime, timedelta


from django.core.mail import send_mail
from django.conf import settings

from apps.sipmanager import sip_task

import models


WATCHDOG_LAST_RUN = 0


class Watchdog(sip_task.SipTask):
    """
    Ensures no process have been idle to long, will send mail if any found.
    
    for testing use mail adr = ''
    """
    SHORT_DESCRIPTION = 'watchdog'
    PRIORITY = sip_task.SIP_PRIO_HIGH
    
    def prepare(self):
        global WATCHDOG_LAST_RUN
        
        if not WATCHDOG_LAST_RUN:
            # starting up send initial msg to ensure setup is ok
            self.log('Mailing admins Watchdog is starting', 3)
            for eadr in settings.ADMIN_EMAILS:
                self.send_email(eadr, 'Thumbler notification', 'Watchdog is starting.')
            
        if WATCHDOG_LAST_RUN + 300 > time.time():
            # only check and potentionally mail out every five mins
            return False
        WATCHDOG_LAST_RUN = time.time()
        return True
        
        
    def run_it(self):
        global WATCHDOG_LAST_RUN
        self.log('Watchdog checking for stale procs', 8)
        cut_of_time = datetime.now() - timedelta(hours=1)
        stale_procs = models.ProcessMonitoring.objects.filter(last_change__lt = cut_of_time)
        proc_count = len(stale_procs)
        if not proc_count:
            return False

        WATCHDOG_LAST_RUN = time.time() + 3600 # only send notification once/hour
        proc_names = []
        for proc_info in stale_procs:
            proc_names.append(proc_info.pid)
        msg = 'Watchdog detected (%i) stale processes: %s ' % (proc_count, ', '.join(proc_names))
        self.log(msg , 1)
        
        for eadr in settings.ADMIN_EMAILS:
            self.send_email(eadr, 'Thumbler warning!', msg)
        
    
        
    

task_list = [
    Watchdog,
]

