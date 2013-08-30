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
"""

import codecs
import sys
import os.path
import time

from django.conf import settings


#from django.core.mail import send_mail


class LogIt(object):
    
    def __init__(self, log_lvl=999, log_file='', print_log=False ):
        self._log_lvl = max(log_lvl, 0) # self.debug_lvl
        self._log_file = log_file # SIP_LOG_FILE
        if log_file:
            log_dir = os.path.dirname(log_file)
            if not os.path.exists(log_dir):
                try:
                    os.makedirs(log_dir)
                except:
                    raise IOError('Failed to log directory: %s' % log_dir)
                print 'Created logdir: %s' % log_dir
            self._log_print = print_log  # settings.PRINT_LOG
        else:
            self._log_print = True # we need atleast one of them
        self._log_skip_time_stamp = False # logstrings that dont do lf temp enables this...
                                          # self.skip_time_stamp
        
                                          
    def log(self, msg, lvl=1, skip_lf=False,mail_it=False):
        if self._log_lvl < lvl:
            return
        if self._log_print:
            if skip_lf:
                print msg,
                sys.stdout.flush()
            else:
                print '%i %s' % (lvl, msg)

        if self._log_skip_time_stamp:
            ts = ''
        else:
            ts = '[%s] ' % time.asctime()
        if skip_lf:
            lf = ''
            self._log_skip_time_stamp = True
        else:
            lf = '\n'
            self._log_skip_time_stamp = False
        if self._log_file:
            codecs.open(self._log_file, 'a', 'utf-8').write(u'%s%s%s' % (ts, msg, lf))
        if mail_it: 
            for eadr in settings.ADMIN_EMAILS:
                self.send_email(eadr, 'Thumbler log warning', msg)
            #for eadr in settings.ADMIN_EMAILS:
            #    send_mail('Thumbler log warning', msg, '', [eadr], fail_silently=False)
            
        return
    
