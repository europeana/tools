
import codecs
import os.path
import shutil
import sys

from django.conf import settings

FILE_LISTS_DIR_NAME = 'file_lists'

FILE_LISTS = os.path.join(settings.SIP_OBJ_FILES, FILE_LISTS_DIR_NAME)


BAD_PREFIX = settings.SIP_OBJ_FILES + '/'



def process_file(src_path, rel_fname):
    new_fh = codecs.open(os.path.join(FILE_LISTS, 'sync-wait', rel_fname), 'w', 'utf-8')
    old_fh = codecs.open(os.path.join(src_path, rel_fname), 'r', 'utf-8')
    for line in old_fh.readlines():
        fixed_line = line.split(BAD_PREFIX)[1]
        new_fh.write(fixed_line)
    new_fh.close()
    old_fh.close()
    

def fix_prefixes(src_path):
    for rel_fname in os.listdir(src_path):
        print 'processing %s ...' % rel_fname,
        sys.stdout.flush()
        process_file(src_path, rel_fname)
        print 'Ok!'
    print 'Everything done!'




if __name__ == "__main__":
    if len(sys.argv) == 1:
        print 'params: path to files that should be fixed'
        sys.exit(1)
    fix_prefixes(sys.argv[1])