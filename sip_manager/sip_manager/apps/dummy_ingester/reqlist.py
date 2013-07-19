import datetime
import os
import sys

from django.conf import settings
from django.db import connection


from utils.gen_utils import count_records, db_is_mysql
from apps.dummy_ingester import models


class ReqListBase(object):
    def list_req_in_filessystem(self):
        lst = []
        for s in os.listdir(settings.PATH_COLLECTIONS):
            try:
                int(s[0]) # all collections start with a int
            except:
                continue
            rel_dir = os.path.join(s, 'output_xml')
            output_dir = os.path.join(settings.PATH_COLLECTIONS, rel_dir)
            if not os.path.exists(output_dir):
                continue

            xml_file = ''
            for ext in ('xml','xml.gz'):
                fn = os.path.join(output_dir, '%s.%s' % (s, ext))
                if os.path.exists(fn):
                    xml_file = fn
                    break # this ext found
            if not xml_file:
                continue # no ext variant found

            rel_fn = xml_file.split(settings.PATH_COLLECTIONS)[1][1:]
            lst.append((s, rel_fn))
        return lst

    
class ReqListNewModel(ReqListBase):
    """
    Run once class to add support for identifying and removing classes.
    """
    def __init__(self):
        if db_is_mysql:
            self.sql = "ALTER TABLE `dummy_ingester_request` ADD `pk_reqlist` INT NOT NULL AFTER `id`"
        else:
            self.sql = "ALTER TABLE dummy_ingester_request ADD COLUMN pk_reqlist INT NOT NULL DEFAULT 0"
        
    def run(self):
        req_lst = models.Request.objects.all()
        try:
            i = req_lst[0].pk_reqlist
        except:
            # old db, update it and terminate
            self.update_db()
            print 'Database was obsolete, now synced, please run this one more time!'
            sys.exit(1)

        output_dirs = {}
        for name, rel_path in self.list_req_in_filessystem():
            output_dirs[os.path.split(rel_path)[0]] = name

        for req in req_lst:
            if req.pk_reqlist:
                continue # already set
            output_dir = os.path.split(req.rel_path)[0]
            reqlst_name = output_dirs[output_dir]
            req.pk_reqlist = models.ReqList.objects.get(name=reqlst_name).pk
            req.save()
             
            
    def update_db(self):
        cursor = connection.cursor()
        cursor.execute(self.sql)
        i = cursor.rowcount


class ReqListUpdater(ReqListBase):

    def __init__(self, logger):
        self.logger = logger
        
    def log(self, msg):
        self.logger('REQUEST-UPDATER %s' % msg)
        
    def run(self):
        # remove items no longer pressent
        for rl in models.ReqList.objects.all():
            full_path = os.path.join(settings.PATH_COLLECTIONS, rl.rel_path)
            if not os.path.exists(full_path):
                rl.delete()
                self.log('%s - deleted' % rl.name)

        dir_list = self.list_req_in_filessystem()
        for req_name, req_file in dir_list:
            self.found_req(req_name, req_file)
                                  

    def found_req(self, req_name, req_file, changed=False):
        try:
            rl, created = models.ReqList.objects.get_or_create(rel_path=req_file, name=req_name)
        except:
            self.log('***** Failed to add %s' % req_name)
            raise
        full_path = os.path.join(settings.PATH_COLLECTIONS, req_file)
        if created:
            status = 'added'
        elif rl.file_date != datetime.datetime.fromtimestamp(os.path.getmtime(full_path)):
            status = 'changed'
        else:
            return
        rl.rec_count = count_records(full_path) # make sure count is correct
        rl.save()
        self.log('%s - %s' % (rl.name, status))


                
def requests_processing():
    pass
