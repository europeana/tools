

import glob
import os.path
import string
import sys
import time

from django.conf import settings

from utils.logit import LogIt
from utils.execute_cmd import ExecuteCommand

from utils.glob_consts import CONVERT_COMMAND, TINY_SIZE


RETRIEVE_USERHOST = settings.IMG_RETRIEVE_USERHOST
REMOTE_BASE_DIR = settings.IMG_RETRIEVE_REMOTE_BASE_DIR
LOCAL_BASE_DIR = settings.IMG_RETRIEVE_LOCAL_BASE_DIR
OUTER_HEX_LOOP = settings.OUTER_HEX_LOOP

DIR_COUNT = 16 * 16 * len(OUTER_HEX_LOOP)

HEX_UPPER = string.hexdigits.upper()[:16]


OLD_FULL_DOC_EXT = 'FULL_DOC.jpg'

class ImgRetriever(LogIt, ExecuteCommand):

    def retrieve_images(self):
        print 'Will retrieve images from: %s:%s' % (RETRIEVE_USERHOST, REMOTE_BASE_DIR)
        print 'to:', LOCAL_BASE_DIR
        self.iterate_action(self.sync_dir)
        
    def tinys_from_old(self):
        self.progress_update_timeout()
        print 'Will generate tinys from:', LOCAL_BASE_DIR
        self.tiny_base_dir = settings.SIP_OBJ_FILES + '/TINY'
        print 'Storing it at usual place (%s)' % self.tiny_base_dir
        self.dirs_processed_count = 0
        self.iterate_action(self.tinyfy_dir)
        print
        print 'TINY generation done!'
    
    
    def sync_dir(self, ddir):
        if not ddir:
            sys.exit(1)
        cmd = 'rsync -avP %s:%s/%s/*.%s %s/%s' % (RETRIEVE_USERHOST, REMOTE_BASE_DIR, ddir, OLD_FULL_DOC_EXT, 
                                                            LOCAL_BASE_DIR, ddir)
        retcode, stdout, stderr = self.cmd_execute_output(cmd)
        if retcode:
            print '*********   rsync failure   **********'
            print stdout
            print
            print stderr
            sys.exit(1)
        return ''
        

    def iterate_action(self, f):
        t1_glob = time.time()
        idx = 0
        for c1 in OUTER_HEX_LOOP:
            for c2 in HEX_UPPER:
                for c3 in HEX_UPPER:
                    idx += 1
                    self.ddir = c1 + c2 + c3
                    self.log('Starting %s' % self.ddir, 8)
                    t1_loop = time.time()
                    progress_extra = f(self.ddir)
                    t2_loop = time.time() - t1_loop
                    t2_glob = time.time() - t1_glob
                    
                    #eta_s = t2_glob / idx * DIR_COUNT - t2_glob
                    eta_h, eta_m, eta_s = self.eta_calc(t2_loop * (DIR_COUNT - idx))
                    s = '\t %s done - %is elapsed\t eta: %i:%i:%i' % (self.ddir, int(t2_loop+0.5), eta_h, eta_m, eta_s)
                    if progress_extra:
                        s = '%s - %s' % (s, progress_extra)
                    self.log(s,8)
                    
                    
    def tinyfy_dir(self, ddir):
        self.dirs_processed_count += 1
        self.t_start_ddir = time.time()
        idx_added = 0
        idx = 0
        org_files = glob.glob('%s/%s/*.%s' % (LOCAL_BASE_DIR, ddir, OLD_FULL_DOC_EXT))
        f_count = len(org_files)
        for org_fn in org_files:
            idx += 1
            new_rel_fn = self.modernize_filename(ddir, org_fn)
            if not os.path.exists(self.new_fn_abs(new_rel_fn)):
                self.generate_tiny(org_fn, new_rel_fn)
                idx_added += 1
            self.tinyfy_progress(f_count, idx, idx_added)
            
        return '%i TINY imgs added' % idx_added
        
    
    def tinyfy_progress(self, file_count, idx, tinys_created):
        if not self.is_progress_update_timeout():
            return

        dir_perc_done = float(idx) / file_count
        dir_perc_s = '%.2f' % (dir_perc_done * 100)
        dir_t_elapsed = time.time() - self.t_start_ddir
        dir_t_remain =  (dir_t_elapsed / dir_perc_done) - dir_t_elapsed
        
        tot_dirs_remaining = DIR_COUNT - self.dirs_processed_count 
        tot_t_remain = tot_dirs_remaining * (dir_t_elapsed + dir_t_remain)
        tot_proc = '%.2f' % (float(self.dirs_processed_count) / DIR_COUNT * 100)
        
        dir_eta = self.eta_s(dir_t_remain)
        tot_eta = self.eta_s(tot_t_remain)
        self.log('tot eta: %s (%s)   dir_eta: %s (%s)   %i/%i dirs - %s' % (tot_eta, tot_proc,    
                                                                              dir_eta, dir_perc_s,
                                                                              self.dirs_processed_count,
                                                                              DIR_COUNT,
                                                                              self.ddir),1)
        self.progress_update_timeout()
        
    def modernize_filename(self, ddir, org_fn):
        d, rel_fn = os.path.split(org_fn)
        full_hex = '%s%s' % ( ddir, rel_fn.split('.' + OLD_FULL_DOC_EXT)[0])
        new_fn = '%s/%s/%s.jpg' % (full_hex[:2], full_hex[2:4], full_hex)
        return new_fn
        
    def new_fn_abs(self, fn):
        return '%s/%s' % (self.tiny_base_dir, fn)
    
    def generate_tiny(self, org_fn, new_fn):
        
        # Create needed storage dirs
        dir_1 = '%s/%s' % (self.tiny_base_dir, new_fn[:2])
        dir_2 = '%s/%s' % (dir_1, new_fn[3:5])
        if not os.path.exists(dir_2):
            for ddir in (dir_1, dir_2):
                if not os.path.exists(ddir):
                    os.mkdir(ddir)
        cmd = [CONVERT_COMMAND]
        cmd.append('-resize %ix%i' % TINY_SIZE)
        cmd.append('%s[0]' % org_fn)
        new_abs_fn = self.new_fn_abs(new_fn)
        cmd.append(new_abs_fn)
        retcode, stdout, stderr = self.cmd_execute_output(cmd)
        if retcode:
            print '*********   tiny resize failure   **********'
            print ' '.join(cmd)
            print
            print stdout
            print
            print stderr
            print
            os.remove(new_abs_fn)
            print 'Deleted any stray output to', new_abs_fn
            sys.exit(1)
        return 1

    
    def eta_calc(self, s):
        m = 0
        while s > 59:
            m +=1
            s -= 60
        h = 0
        while m > 59:
            h += 1
            m -= 60
        return h, m, s
    
    def eta_s(self, i):
        return '%i:%i:%i' % self.eta_calc(i)

    def progress_update_timeout(self):
        self.progress_t0 = time.time() + 10

    def is_progress_update_timeout(self):
        return not (time.time() < self.progress_t0)
        

            
            

ir = ImgRetriever(log_lvl=7, log_file=settings.SIP_LOG_FILE, print_log=True)
ir.tinys_from_old()
