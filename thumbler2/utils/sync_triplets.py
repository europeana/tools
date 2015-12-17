
"""
sample only-vancis line:
< 01B590FAD199BDC1F1DB014EA32FA7A5880DE12B42FE82AF49E0AA37F41FBD88.jpg

        Find imgs only on Vancis
        Split it in bundles
        Get one bundle from img1 - send to both any9 servers
        Get one bundle from img2 - send to both any9 servers
"""

import os.path
import sys
import time


import execute_cmd



#VANCIS_HOSTS = ('euadmin@img1.europeana.sara.nl','euadmin@img2.europeana.sara.nl')
#VANCIS_PREFIX = '/repository'

#ANY9_HOSTS = ('thumbler@img1','thumbler@img2')
#ANY9_PREFIX = '/data/thumbler'

IMG_CATEGORIES = ('BRIEF_DOC', 'FULL_DOC')

#LINES_ONLY_VANCIS = 8208658

ETA_INTERVALL = 10



# ssh euadmin@img2.europeana.sara.nl ls  /repository/000 | sort > triplets-000

class ETA(object):
    def __init__(self):
        self.etaReset()

    def etaReset(self, total_count=0, intervall=10):
        self.eta_current_idx = 0
        self._eta_old_count = 0
        self._eta_intervall = intervall
        self.etaSetTotalCount(total_count)

    def etaSetTotalCount(self, i):
        self._eta_total_count = i

    def etaStart(self, total_count=0):
        if total_count:
            self.etaReset(total_count)
        self._eta_time_start = time.time()
        self.etaCalculateNextProgress()

    def etaCalculateNextProgress(self):
        self._eta_next_progress = time.time() + self._eta_intervall

    def etaCalculate(self):
        percent_done = (float(self.eta_current_idx)/self._eta_total_count) * 100
        now = time.time()
        eta = self._eta_time_start + ((now - self._eta_time_start) / (percent_done / 100))
        return percent_done, now, eta

    def etaProgress(self, check_timeout=False):
        if check_timeout and (time.time() < self._eta_next_progress):
            # its up to us to make sure we dont print progress to often..
            return
        percent_done, now, eta = self.etaCalculate()
        print 'remaining: %i since last update: %i \tpercent:%3.2f ETA: %s' % (self._eta_total_count - self.eta_current_idx,
                                                                               self.eta_current_idx - self._eta_old_count,
                                                                               percent_done,
                                                                               time.asctime(time.localtime(eta)))
        self._eta_old_count = self.eta_current_idx
        if check_timeout:
            self.etaCalculateNextProgress()
        return






class Foo(ETA):
    def readOnlyVancis(self):
        fout = open('/Users/jaclu/tmp/imglists/vancis-only-full-names','w')
        self.etaStart(reset=True)
        t1 = time.time() + ETA_INTERVALL
        for line in open('/Users/jaclu/tmp/imglists/only-vancis').readlines():
            self.eta_current_idx += 1
            # < 01B590FAD199BDC1F1DB014EA32FA7A5880DE12B42FE82AF49E0AA37F41FBD88.jpg
            # to  01/B5/01B590FAD199BDC1F1DB014EA32FA7A5880DE12B42FE82AF49E0AA37F41FBD88.jpg
            fname = line.split()[1]
            rel_name = os.path.join(fname[:2], fname[2:4], fname)
            for img_cat in IMG_CATEGORIES:
                fname = os.path.join(img_cat, rel_name)
                fout.write('%s\n' %fname)

            if time.time() > t1:
                self.etaProgress()
                t1 = time.time() + ETA_INTERVALL
        fout.close()






class SyncFromFileList(ETA,execute_cmd.ExecuteCommand):
    def run(self, file_lists, host_source, host_dest):
        self.host_source = host_source
        self.host_dest = host_dest
        ffiles = os.listdir(file_lists)
        ffiles.sort()
        if not len(ffiles):
            print
            print '*****   ERROR'
            print 'No batch files fond'
            sys.exit(4)
        batch_count = len(ffiles)
        done_dir = os.path.join(os.path.split(file_lists)[0],'img_sync_done')
        print 'Processing the files in %i batches' % batch_count
        print ' once done batch files will be moved to', done_dir
        self.etaStart(total_count=batch_count)
        for batch_file in ffiles:
            self.eta_current_idx += 1
            listoffiles = os.path.join(file_lists,batch_file)
            self.sendBatch(listoffiles)
            self.etaProgress(check_timeout=True)
            try:
                new_fname = os.path.join(done_dir, batch_file)
                os.rename(listoffiles,new_fname)
            except:
                print
                print '*****    ERROR: Failed to move batchfile'
                print '%s -> %s' % (listoffiles, new_fname)
                sys.exit(3)
        print
        print 'Completed file sync with no errors!'
        return


    def sendBatch(self,batch_file):
        triplet = batch_file.split('-')[-1]
        if len(triplet) != 3:
            print '****** Error failed to extract triplet from batchfile'
            print ' batchfile:       ', batch_file
            print ' bad triplet name:', triplet
            sys.exit(2)
        cmd = 'rsync -avP --files-from=%s %s %s' % (batch_file, os.path.join(self.host_source, triplet), os.path.join(self.host_dest, triplet))
        retcode,stdout,stderr = self.cmd_execute_output(cmd, timeout=999)
        if retcode:
            print '============= Command reported error  ====='
            stdout_file = '/tmp/filesync-error-stdout'
            stderr_file = '/tmp/filesync-error-stderr'
            open(stdout_file,'w').write(stdout)
            open(stderr_file,'w').write(stderr)
            print 'Check %s  and  %s  for details' % (stdout_file, stderr_file)
            print 'Retcode:', retcode
            print '-----'
            print 'cmd was:', cmd
            sys.exit(2)
        return


    def log(self, msg, lvl):
        pass


if __name__ == "__main__":
    #c = SyncFromFileList('euadmin@img1.europeana.sara.nl:/repository', '/Users/jaclu/tmp/foo/test_syncs/parts',)
    if len(sys.argv) != 4:
        print 'Usage: sync_triplets dir_with_parts_files source_location   destination'
        sys.exit(1)


    c = SyncFromFileList()
    c.run(*sys.argv[1:])
    #'/Users/jaclu/tmp/foo/test_syncs/parts', '/Users/jaclu/tmp/foo/100OLYMP', 'thumbler@img1:/data/jacob')
