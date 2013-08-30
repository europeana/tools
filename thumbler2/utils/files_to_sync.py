import codecs
import glob
import os.path
import shutil

from django.conf import settings
from django.core.mail import send_mail


class FileSyncException(Exception):
    """
    Dont forget:
    from django.conf import settings
    from django.core.mail import send_mail
    """
    def __init__(self, msg='', *args, **kwargs):
        self.msg = msg
        for eadr in settings.ADMIN_EMAILS:
            if eadr:
                send_mail('Thumblr2 FileSyncException', self.msg, '', [eadr], fail_silently=False)
    
    def __str__(self):
        return repr(self.msg)

class ImproperlyConfigured(Exception):
    pass



# Since looking up in settings takes some extra cpu, important settings
# are cached within the module
NODE_NAME = settings.NODE_NAME

SIP_OBJ_FILES = settings.SIP_OBJ_FILES


DIR_ST_ADDING = 'adding'
DIR_ST_SYNC_WAIT = 'sync-wait'
DIR_ST_DELIVERED = 'delivered'

FILE_LISTS_DIR_NAME = 'file_lists'

FILE_LISTS = os.path.join(SIP_OBJ_FILES, FILE_LISTS_DIR_NAME)

SYNC_FILE_PREFIX = 'thmblr'


if not os.path.exists(FILE_LISTS):
    try:
        os.makedirs(FILE_LISTS)
    except:
        raise ImproperlyConfigured('Failed to create missing directory - FILE_LISTS: %s' % FILE_LISTS)

for s in (DIR_ST_ADDING, DIR_ST_DELIVERED, DIR_ST_SYNC_WAIT):
    ddir = os.path.join(FILE_LISTS, s)
    if not os.path.exists(ddir):
        try:
            os.makedirs(ddir)
        except:
            raise ImproperlyConfigured('Failed to create dir: %s' % ddir)



class FilesToSync(object):
    """Manipulate file lists for syncing between nodes"""
    PREFIX_TO_REMOVE = '%s/' % SIP_OBJ_FILES

   
    def set_state(self, state, org_fname):
        """Moves a file from (any valid state) to indicated state"""
        org_path, rel_fname = os.path.split(org_fname)
        base_dir, old_state = os.path.split(org_path)
        if not self.state_ok(old_state):
            raise FileSyncException('set_state failed, file didnt come from a valid state dir: %s' % org_fname)
        if not base_dir == FILE_LISTS:
            raise FileSyncException('set_state failed, base_dir not valid for source: %s' % org_fname)
        if not self.state_ok(state):
            raise FileSyncException('set_state failed, new state [%s] invalid for file: %s' % (state, org_fname))
        if not rel_fname:
            raise FileSyncException('set_state failed, no relative file-name detected: %s' % org_fname)

        new_base_path = os.path.join(FILE_LISTS, state)
        new_fname = os.path.join(new_base_path, rel_fname)
        
        if os.path.exists(new_fname):
            # avoid coliding with a duplicate by renaming
            base_name, ext = os.path.splitext(rel_fname)        
            new_rel = base_name + 'x' + ext
            new_fname = os.path.join(new_base_path, new_rel)
            
        if not os.path.exists(org_fname):
            return # not much to be done 
        if os.path.exists(new_fname):
            raise FileSyncException('set_state failed, new filename already being used. old: %s \tnew: %s' % (org_fname, new_fname))
        shutil.move(org_fname, new_fname)
        
    def list_files_in_state(self, state):
        "returns a list of files in a state"
        if not self.state_ok(state):
            raise FileSyncException('list_files_in_state failed, state invalid: %s' % state)
        abs_fnames = glob.glob(os.path.join(FILE_LISTS, state,'*.%s' % SYNC_FILE_PREFIX))
        return abs_fnames

        
    def state_ok(self, state):
        if state in (DIR_ST_ADDING, DIR_ST_SYNC_WAIT, DIR_ST_DELIVERED):
            return True
        else:
            return False
            
    def img_fname_filter_prefix(self, fname):
        foo, rel_fname = fname.split(self.PREFIX_TO_REMOVE)
        return rel_fname



class AddingFilesToSync(FilesToSync):
    """Add list of files for later sync processing"""
    
    def __init__(self, suffix, logger):
        filename = '%s-%s.%s' % (NODE_NAME, suffix, SYNC_FILE_PREFIX)
        self.log = logger
        self.abs_filename = os.path.join(FILE_LISTS, DIR_ST_ADDING, filename)
        self.log_msg = '>> %s' % self.abs_filename # since its static pre-generate for performance
    
    def add(self, fname):
        rel_fname = self.img_fname_filter_prefix(fname)
        self.log(self.log_msg, 8)
        codecs.open(self.abs_filename, 'a', 'utf-8').write('%s\n' % rel_fname)
        
    def close_adding(self):
        "No more files to add"
        # Move file list from running to SyncWait
        self.set_state(DIR_ST_SYNC_WAIT, self.abs_filename)


