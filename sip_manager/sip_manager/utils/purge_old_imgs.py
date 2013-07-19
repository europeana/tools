import os
import string
import sys

from settings import SIP_OBJ_FILES, OBJ_OLDS

from utils.execute_cmd import ExecuteCommandWithLog

class PurgeOldImgs(ExecuteCommandWithLog):
    IMG_CATEGORIES = ( #'BRIEF_DOC',
                       #'FULL_DOC',
                       'TINY',
                 )
    HEXCHARS = '0123456789ABCDEF'
    CUT_OF_AGE_DAYS = 60
    MOVE_MODE = False  # if true old imgs are moved to OBJ_OLDS dir
    
    def run(self):
        self.mkdir_existed = 0
        if self.MOVE_MODE:
            self.create_destinations()
        for self.img_cattegory in self.IMG_CATEGORIES:
            
            self.source_base = '%s/%s' % (SIP_OBJ_FILES, self.img_cattegory)
            self.dest_base = '%s/%s' % (OBJ_OLDS, self.img_cattegory)
            self.loop_the_dirs()
                  
                  
    def loop_the_dirs(self):
        for c1 in self.HEXCHARS:
            for c2 in self.HEXCHARS:
                dir1 = c1 + c2
                #if (self.img_cattegory == 'FULL_DOC') and (dir1 < 'C5'):
                #    continue
                for c3 in self.HEXCHARS:
                    for c4 in self.HEXCHARS:
                        dir2 = c3 + c4
                        workdir = dir1 + '/' + dir2
                        self.do_one_sub_dir(workdir)
        
        
    def do_one_sub_dir(self, ddir):
        self.log('Processing %s/%s' % (self.img_cattegory, ddir), 4)
        source_dir = os.path.join(self.source_base, ddir)
        if not os.path.exists(source_dir):
            return
        if self.MOVE_MODE: # move
            dest_dir = os.path.join(self.dest_base, ddir)
            cmd = 'find %s/*.jpg -mtime +%i -exec mv -f {} %s \;' % (source_dir, self.CUT_OF_AGE_DAYS, dest_dir)
        else: # rm
            cmd = 'find %s/*.jpg -mtime +%i -exec rm {}  \;' % (source_dir, self.CUT_OF_AGE_DAYS)
        retcode,stdout,stderr = self.cmd_execute_output(cmd)
        if retcode:
            print '***** Command failed with code', retcode
            print '========== stdout ============'
            print stdout
            print '========== stderr ============'
            print stderr
            sys.exit(1)
        return
    
    

    #
    #  Stuff to ensure the destination file trees exist
    # 
        
    def create_destinations(self):
        for img_cattegory in self.IMG_CATEGORIES:
            ddir = os.path.join(OBJ_OLDS, img_cattegory)
            self.safe_mkdir(ddir)
            for c1 in self.HEXCHARS:
                for c2 in self.HEXCHARS:
                    lev1_dir = c1 + c2
                    self.safe_mkdir(os.path.join(ddir, lev1_dir))
                    for c3 in self.HEXCHARS:
                        for c4 in self.HEXCHARS:
                            lev2_dir = c3 + c4
                            self.safe_mkdir(os.path.join(ddir, lev1_dir, lev2_dir))
                    
    def safe_mkdir(self, ddir):
        try:
            os.mkdir(ddir)
            self.log('Created %s' % ddir, 3)
        except OSError,e:
            if e.errno == 17:
                self.mkdir_existed += 1
                pass
            else:
                raise OSError,e
        return True
                  


if __name__ == "__main__":
    po = PurgeOldImgs(log_lvl=5)
    po.run()
    
    