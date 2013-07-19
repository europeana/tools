import codecs
import sys
import time


class SimpleLog(object):
    def __init__(self, log_lvl=999, log_file='', print_log=False ):
        self._log_lvl = log_lvl # self.debug_lvl
        self._log_file = log_file # SIP_LOG_FILE
        if log_file:
            self._log_print = print_log  # settings.PRINT_LOG
        else:
            self._log_print = True # we need atleast one of them
        self._log_skip_time_stamp = False # logstrings that dont do lf temp enables this...
                                          # self.skip_time_stamp
        
                        
    def log(self, msg, lvl=1, skip_lf=False):
        if self._log_lvl < lvl:
            return
        if self._log_print:
            if skip_lf:
                print msg,
                sys.stdout.flush()
            else:
                print msg

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
        return



