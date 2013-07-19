

import os
import shutil
import sqlite3
import string
import subprocess
import sys
import time

proj_root = os.path.split(os.path.dirname(__file__))[0]
sys.path.insert(0, proj_root)

from utils.gen_utils import calculate_hash

# imgs in objects.xml 11 120 315
# imgs in all_listing  9 928 515


# grep "arr name"  objects.xml > euobjs

WORK_DIR = '/Users/jaclu/tmp/deadfiles'

INTERVALL_HASHIFY = 1000

COUNT_OBJECTS = 11120315
UNIQUE_OBJS = 19983698
COUNT_AVAILABLE_FILES = 20039210

ACCEPTABLE_IMG_EXTS = ('jpg', 'png', 'gif')


DIF_BAD_EXT = 'bad_ext'
DIF_BAD_EXT_COUNT = 'bad_ext_count'
DIF_MISSING_FILES = 'missing_files'
DIF_NOT_IN_PORTAL = 'not_in_portal'

class Progress(object):
    
    def __init__(self, intervall=10, label='', steps=0, display=True):
        self.TASK_PROGRESS_TIME = intervall
        self._task_label = label
        self._show_date_limit = 60 * 60 * 20 # etas further than this will display date
        self.task_starting(label, steps)
        

    def log(self, msg):
        print msg
        #open(SIP_LOG_FILE,'a').write('%s\n' % msg)


    # ==========   Task steps   ====================

    def task_starting(self, label, steps=0, display=True):
        "new subtask starting, give at label and if possible no of steps."
        self._task_time_start = time.time()
        self._task_steps = steps
        self._task_previous = 0

        if label:
            self._task_label = label
        self._task_progress = ''
        self._task_eta = ''
        if display:
            self.task_progress(0)
        self._task_show_time = time.time()


    def task_time_to_show(self, progress='', terminate_on_high_load=False):
        """Either use as a bool check, or give a param directly.

        A number param is sent to task_progess()
        a string param is used directly.

        remember if terminate_on_high_load=True a SipSystemOverLoaded exception
        will be triggered on high load, you need to catch that in you code
        if you need to do any cleanup
        """
        if self._task_show_time + self.TASK_PROGRESS_TIME < time.time():
            if progress:
                if isinstance(progress, int):
                    self.task_progress(progress)
                else:
                    self._task_progress = progress
                    self.log('%s - %s' % (self._task_label,
                                               self._task_progress))
            self._task_show_time = time.time()
            b = True
        else:
            b = False
        return b


    def task_progress(self, step):
        "update stepcount and eta (from last call to task_starting()."
        if self._task_steps and step: # avoid zero div
            perc_done, self._task_eta = self._task_calc_eta(step)
            since_last = step - self._task_previous
            self._task_previous = step
            self._task_progress = '%i/%i %i  (%0.2f%%)' % (step, self._task_steps,
                                                             since_last, perc_done)
        else:
            if self._task_steps:
                self._task_progress = '%i items' % self._task_steps
            else:
                self._task_progress = '%s' % step
            self._task_eta = 'unknown'
        self.log('%s  -  %s  eta: %s' % (self._task_label, self._task_progress,
                                              self._task_eta))


    def _task_calc_eta(self, step):
        percent_done = float(step) / self._task_steps * 100
        elapsed_time = time.time() - self._task_time_start
        eta_t_from_now = int(elapsed_time / ((percent_done / 100) or 0.001))
        eta = self._task_time_start + eta_t_from_now
        if (eta - time.time()) < self._show_date_limit:
            eta_s = time.strftime('%H:%M:%S', time.localtime(eta))
        else:
            eta_s = time.strftime('%m-%d %H:%M:%S', time.localtime(eta))
        return percent_done, eta_s

    



#
#========================
#



class UnusedFinder(object):
    def __init__(self):
        self.fn_active_unsorted = WORK_DIR + '/active_files.unsorted'
        self.fn_active_sorted = WORK_DIR + '/active_files.sorted'
        self.fn_active_nodupes = WORK_DIR + '/active_files' 
        self.fn_active_tree = WORK_DIR + '/active_tree'

        self.fn_avail_on_disk = WORK_DIR + '/available_files'
        self.fn_avail_tree = WORK_DIR + '/available_tree'
        
        self.fn_baditems_tree = WORK_DIR + '/bad_tree'
        
        self.db = sqlite3.connect(WORK_DIR + '/sqlite3.db')
        
        
    def active_imgs_hashify(self, fname):
        cur = self.db.cursor()
        cur.execute('DROP TABLE IF EXISTS europeana_objs')
        cur.execute("""CREATE TABLE europeana_objs 
            (hash TEXT PRIMARY KEY, 
            url TEXT,
            no_file INTEGER DEFAULT 0)""")
        self.db.commit()
        print 
        print '====  hashifying active imgs, saving to %s   ===' % self.fn_active_unsorted
        progr = Progress(steps=COUNT_OBJECTS, label='hashifying')
        #f_out = open(self.fn_active_unsorted, 'w')
        f = open(fname)
        i = 0 ; j = 0
        for line in f:
            src = line.split('<str>')[1].split('</str>')[0]
            h = calculate_hash(src)
            url_hash = '%s/%s' % (h[:3], h[3:])
            cur.execute('INSERT OR IGNORE INTO europeana_objs VALUES ("%s","%s")' % (src, url_hash))
            #f_out.write('%s\n' % url_hash)
            i += 1 ; j += 1
            if j > 1000:
                j = 0
                progr.task_time_to_show(i)
        f.close()
        self.db.commit()
        #f_out.close()
        cur.close()
        print '  Done! - saved in %s' % self.fn_active_unsorted
        
    def active_imgs_sort(self):
        print # aprox 10 mins
        print '====  Sorting active files from %s  =====' % self.fn_active_unsorted
        t = time.time()
        print 'starting %s will take aprox 10 mins' % time.strftime('%H:%M:%S', time.localtime(t))
        self.cmd_execute1('sort %s > %s' % (self.fn_active_unsorted, 
                                            self.fn_active_sorted))
        print 'done     %s  - seconds to complete:  %i' % (time.strftime('%H:%M:%S', time.localtime(time.time())),
                                                time.time() - t)
        print 'saved in %s' % self.fn_active_sorted
        
    def active_dup_removal(self):
        print
        print '====  Removing dupes from active files to %s  =====' % self.fn_active_nodupes
        t = time.time()
        print 'starting %s' % time.strftime('%H:%M:%S', time.localtime(t))
        self.cmd_execute1('cat %s | uniq > %s' % (self.fn_active_sorted, 
                                                  self.fn_active_nodupes))
        print 'done     %s  - seconds to complete:  %i' % (time.strftime('%H:%M:%S', time.localtime(time.time())),
                                                time.time() - t)
        print 'saved in %s' % self.fn_active_nodupes
        
    def active_tree_create(self):
        print 
        print '====  copying active imgs to tree %s   ===' % self.fn_active_tree
        self.tree_create('act_tree', 
                         self.fn_active_nodupes, 
                         item_count=UNIQUE_OBJS,
                         tree_name=self.fn_active_tree)
        
        
        
    def avail_create(self, fname):
        print 
        print '====  reading file tree, saving to %s   ===' % self.fn_avail_on_disk
        cur = self.db.cursor()
        cur.execute('DROP TABLE IF EXISTS files')
        cur.execute("""CREATE TABLE files 
             (hash TEXT PRIMARY KEY, 
             fname TEXT, 
             is_active INTEGER DEFAULT 0, 
             bad_ext INTEGER)""")
        self.db.commit()
        f = open(fname)
        #f_out = open(self.fn_avail_on_disk, 'w')
        cur_dir = ''
        progr = Progress(steps=COUNT_AVAILABLE_FILES, label='avails create')
        i = 0 ; j = 0
        for line in f:
            if not line:
                continue
            if len(line) < 8:
                continue # skip empty drinames
            if line.find('/repository') > -1:
                cur_dir = line.split('/')[-1][:3]
                continue
            f_out.write('%s/%s' % (cur_dir, line))
            i += 1 ; j += 1
            if j > 1000:
                # only do this every second, since there are two imgs for each item
                j = 0
                progr.task_time_to_show(i)
        f_out.close()
        f.close()
        print '  Done! - saved in %s' % self.fn_avail_on_disk
        
        
    def avail_tree_create(self):
        print 
        print '====  copying available imgs to tree %s   ===' % self.fn_avail_on_disk
        self.tree_create('avail_tree', 
                         self.fn_avail_on_disk, 
                         item_count=COUNT_AVAILABLE_FILES,
                         tree_name=self.fn_avail_tree)
        
    def find_difs(self):
        shutil.rmtree(self.fn_baditems_tree, ignore_errors=True)
        os.mkdir(self.fn_baditems_tree)
        self.count_europeana_objects = 0
        self.count_image_files = 0
        self.ok_thumbs = 0
        self.bad_not_in_portal = 0
        self.bad_ext_count = 0
        self.bad_exts = 0
        self.missing_files = 0
        self.bad_already_displayed = ['jpg','error','png'] # mention a few we know of and dont want displayed
        hexdigits = '0123456789ABCDEF'
        progr = Progress(steps=4096, label='find difs')
        i = 0
        for s in hexdigits: 
            for s2 in hexdigits: 
                for s3 in hexdigits:
                    i += 1
                    prefix = s + s2 + s3
                    #if prefix > '017':
                    #    continue
                    self.diffscan_prefix(prefix)
                    progr.task_time_to_show(i)
        print 'Done!'
        print
                    
        print 'Total count Europeana:object', self.count_europeana_objects
        print 'Total count image files     ', self.count_image_files
        print
        
        print 'europeana:object with no file ', self.missing_files # europeana_objs no_file
        print 'nr files not in portal        ', self.bad_not_in_portal # files is_active
        print 'nr imgs with wrong nr of files', self.bad_ext_count
        print 'nr files with bad extentions  ', self.bad_exts # files bad_ext
        print
        print 'ok europeana:objects          ', self.ok_thumbs
        print
        cmd = 'cat %s/%s-??? > %s/summary-%s'
        self.cmd_execute1(cmd % (self.fn_baditems_tree, DIF_BAD_EXT, self.fn_baditems_tree, DIF_BAD_EXT))
        self.cmd_execute1(cmd % (self.fn_baditems_tree, DIF_BAD_EXT_COUNT, self.fn_baditems_tree, DIF_BAD_EXT_COUNT))
        self.cmd_execute1(cmd % (self.fn_baditems_tree, DIF_MISSING_FILES, self.fn_baditems_tree, DIF_MISSING_FILES))
        self.cmd_execute1(cmd % (self.fn_baditems_tree, DIF_NOT_IN_PORTAL, self.fn_baditems_tree, DIF_NOT_IN_PORTAL))
        print 'Summary files saved - Done!'
        
    def diffscan_prefix(self, prefix):
        actives = open(self.fn_active_tree + '/%s' % prefix).readlines()
        self.count_europeana_objects += len(actives)
        avails = open(self.fn_avail_tree + '/%s' % prefix).readlines()
        self.count_image_files += len(avails)
        avails_k = {}
        for avail in avails:
            k = avail.split('.')[0] + '\n'
            suffix = '.'.join(avail.split('.')[1:])[:-1]
            if k not in avails_k.keys():
                avails_k[k] = []
            avails_k[k].append(suffix)
            
        
        not_in_portal = [] # hashes in filesystem not used by portal        
        bad_exts = [] #  bad extentions
        bad_ext_count = []
        for k in avails_k.keys():
            img_ok = True
            extentions = avails_k[k]
            
            # Finding not used files
            if k not in actives:
                img_ok = False
                for ext in extentions:
                    not_in_portal.append('%s.%s\n' % (k[:-1], ext))
            if not img_ok:
                continue # since already reported dont check further
            
            
            # checking that the extentions ('versions') of each file is 
            # correct also logging if wrong no of versions
            ok_count = 0
            for ext in extentions:
                if self.is_valid_extention(ext):
                    ok_count += 1
                else:
                    bad_exts.append('%s.%s\n' % (k[:-1], ext))
            if ok_count != 2:
                img_ok = False
            if (ok_count == 1) or (ok_count >2):
                # log when not the expexted two versions are pressent
                extentions.sort()
                bad_ext_count.append('%s - %s\n' % (k[:-1], '  '.join(extentions)))
                
            if img_ok:
                self.ok_thumbs += 1
        if not_in_portal:
            not_in_portal.sort()
            self.bad_not_in_portal += len(not_in_portal)
            open(self.fn_baditems_tree + '/%s-%s' % (DIF_NOT_IN_PORTAL, prefix), 'w').writelines(not_in_portal)        
        if bad_ext_count:
            bad_ext_count.sort()
            self.bad_ext_count += len(bad_ext_count)
            open(self.fn_baditems_tree + '/%s-%s' % (DIF_BAD_EXT_COUNT, prefix), 'w').writelines(bad_ext_count)
        if bad_exts:
            bad_exts.sort()
            self.bad_exts += len(bad_exts)
            open(self.fn_baditems_tree + '/%s-%s' % (DIF_BAD_EXT, prefix), 'w').writelines(bad_exts)
        

        missing_files = []        
        for act in actives:
            if act not in avails_k.keys():
                missing_files.append(act)
        if missing_files:
            self.missing_files += len(missing_files)
            open(self.fn_baditems_tree + '/%s-%s' % (DIF_MISSING_FILES, prefix), 'w').writelines(missing_files)
        
        
    #
    #========================
    #
    def is_valid_extention(self, ext):
        b = True
        parts = ext.split('.')
        if len(parts) != 2:
            b = False
        elif parts[0] not in ('BRIEF_DOC', 'FULL_DOC'):
            b = False
        elif parts[1] not in ACCEPTABLE_IMG_EXTS:
            b = False
        if not b:
            self.display_bad_ext(ext)
        return b
    
    def display_bad_ext(self, ext):
        if ext not in self.bad_already_displayed:
            self.bad_already_displayed.append(ext)
            dash_parts = ext.split('.')[0].split('-')
            if len(dash_parts) == 2 and dash_parts[0] in ('BRIEF_DOC', 'FULL_DOC'):
                try:
                    int(dash_parts[1])
                    return False # just a leftover from a multipage sourcefile, dont bother to display
                except:
                    pass
            print '** new bad ext', ext
    
    
    def tree_create(self, label, src_file, item_count, tree_name):
        shutil.rmtree(tree_name, ignore_errors=True)
        os.mkdir(tree_name)
        progr = Progress(steps=item_count, label=label)
        f_in = open(src_file)
        f_out = None
        i = 0 ; j = 0
        cur_prefix = ''
        for line in f_in:
            prefix = line.split('/')[0]
            if prefix <> cur_prefix:
                cur_prefix = prefix
                if f_out:
                    f_out.close()
                f_out = open(tree_name + '/%s' % cur_prefix, 'w')
            f_out.write(line)
            i += 1 ; j += 1
            if j > 1000:
                j = 0
                progr.task_time_to_show(i)
        f_in.close()
        f_out.close()
        print '  Done!'
        
        
    
    def cmd_execute1(self, cmd):
        "Returns 0 on success, or error message on failure."
        result = 0
        retcode, stdout, stderr = self.cmd_execute_output(cmd)
        if retcode or stdout or stderr:
            result = u'retcode: %s' % retcode
            if stdout:
                result += u'\nstdout: %s' % stdout
            if stderr:
                result += u'\nstderr: %s' % stderr
        return result


    def cmd_execute_output(self, cmd):
        "Returns retcode,stdout,stderr."
        if isinstance(cmd, (list, tuple)):
            cmd = ' '.join(cmd)
        try:
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            s_out, s_err = p.communicate()
            retcode = p.returncode
            stdout = smart_unicode(s_out, errors='replace')
            stderr = smart_unicode(s_err, errors='replace')
        except:
            retcode = 1
            stdout = u''
            stderr = u'cmd_execute() exception - shouldnt normally happen'
        return retcode, stdout, stderr








if __name__ == "__main__":
    uf = UnusedFinder()
    #uf.active_imgs_hashify('/Users/jaclu/Downloads/euobjs')
    #uf.active_imgs_sort()
    #uf.active_dup_removal()
    #uf.active_tree_create()
    #uf.avail_create('/Users/jaclu/Downloads/all_imgs_listing')
    #uf.avail_tree_create()
    uf.find_difs()
    """
    active 2461
    available 4893
    """
