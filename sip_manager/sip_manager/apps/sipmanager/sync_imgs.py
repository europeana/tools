import os
import sys
import time

from django.core import exceptions
from django.conf import settings


from apps.sipmanager import sip_task

LOGLVL_IMG_SYNC_DETAILS = 7

HEXDIGITS = '0123456789ABCDEF'
TOUCH_FILE = settings.IMG_SYNC_TOUCHFILE

class ImgSyncer(sip_task.SipTask):

    def touch(self, fname, times=None):
        with file(fname, 'a'):
            os.utime(fname, times)
        
    def sync_thing(self, source, dest):
        for a in HEXDIGITS:
            for b in HEXDIGITS:
                ddir = '%s%s' % (a,b)
                full_source = '%s/%s/' % (source,ddir)
                full_dest = '%s/%s' % (dest, ddir)
                cmd = 'rsync -avP %s %s' % (full_source, full_dest)
                self.log('Syncing to %s ...' % full_dest, LOGLVL_IMG_SYNC_DETAILS, skip_lf=True)
                t0 = time.time()
                retcode,stdout,stderr = self.cmd_execute_output(cmd, 450)
                self.log('done (%0.1fs)' % (time.time() - t0), LOGLVL_IMG_SYNC_DETAILS)
                if retcode:
                    print '*** Code returned', retcode
                    print stderr
                    self.log('***  rsync returned error', 1)
                    self.log('retcode: %s' % retcode, 1)
                    self.log('stdout: %s' % stdout, 1)
                    self.log('stderr: %s' % stderr, 1)
                    self.done(1)
                pass
           
    def run(self, source_base, dest_base):
        if not settings.SYNC_THINGS:
            raise exceptions.ImproperlyConfigured('Missing setting SYNC_THINGS')
        self.lock_file = TOUCH_FILE + '.lock'
        retcode,stdout,stderr = self.cmd_execute_output('lockfile -r 0 %s' % self.lock_file, 10)
        if retcode:
            self.log('***  sync_imgs failed to start   ***',1)
            self.log(stderr, 1)
            print stderr
            self.done(1, clear_lock_file=False)
            
        self.log('will rsync %s --> %s' % (source_base, dest_base), 1)
        if not isinstance(dest_base, (list,tuple)):
            dest_base = [dest_base]
        for one_dest_base in dest_base:
            for thing in settings.SYNC_THINGS:
                self.log('Syncing images: %s to: %s' % (thing, one_dest_base), 1)
                self.sync_thing('%s/%s' % (source_base, thing),
                                '%s/%s' % (one_dest_base, thing))
        self.touch(TOUCH_FILE)
        self.done()
        
    def done(self, err_lvl=0,clear_lock_file=True):
        if clear_lock_file:
            try:
                os.remove(self.lock_file)
            except:
                pass
        if err_lvl:
            sys.exit(err_lvl)
