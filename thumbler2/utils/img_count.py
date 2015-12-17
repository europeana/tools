

"""

   Iterate over all edm:objects

       use hash of url

       if found inc OBJS_FOUND - add to hash on file
       if not found OBJS_WITHOUT - add to hash on file

    count images found

    IMGS_DEAD = count - OBJS_FOUND


"""


import fileinput
import hashlib
import pickle
import sqlite3
import time


from img_count_settings import *

TOT_COUNT_EDM = 39276887

def timestamp():
    return time.strftime("%Y-%m-%d %H:%M:%S")


# read imgs into a hash





class ThumbNailHandling(object):
    def __init__(self):
        self.conn = sqlite3.connect(SQL_FILE)
        self.c = self.conn.cursor()
        self.f_items_found = open(OBJS_FOUND, 'a')
        self.f_items_not_found = open(OBJS_NOT_FOUND, 'a')
        self.startup_time = time.time()


    def match_edm_objs(self):
        print '===  Match edm objs'
        count = items_found = items_not_found = 0
        t1 = time.time()
        for line in fileinput.input([EDM_FILE]):
            if not line[:2] == 'ht':
                continue
            count += 1
            url = line.strip()
            key = self.hash_url(url)
            self.c.execute('SELECT is_used FROM imgs WHERE img="%s"' % key)
            result = self.c.fetchone()
            if bool(result):
                items_found += 1
                is_already_found = result[0]
                self.f_items_found.write(line)
                if not is_already_found:
                    self.c.execute('UPDATE imgs SET is_used=1 WHERE img="%s"' % key)
            else:
                items_not_found += 1
                self.f_items_not_found.write(line)

            if time.time() > t1 + 15:
                eta = self.eta(count)
                total_elapsed_time = time.time() - self.startup_time
                print '%i\tRate: %i\tETA: %s\tFound: %i\tNot found: %i' % (count, int(count/total_elapsed_time), eta,items_found, items_not_found)
                self.f_items_found.flush()
                self.f_items_not_found.flush()
                self.conn.commit()
                t1 = time.time()

        self.conn.commit()
        self.conn.close()
        print 'Done - final count'
        print '%i\t\tFound: %i\t\tNot found: %i' % (count, items_found, items_not_found)

    def eta(self, count):
        run_time = time.time() - self.startup_time
        done = count / TOT_COUNT_EDM
        remaining = TOT_COUNT_EDM - done
        rate = count/run_time
        time_remaining = (remaining/rate)/3600
        return '%.2fh' % time_remaining



    def hash_url(self, url):
        r_hash = hashlib.sha256(url).hexdigest().upper()
        return r_hash



    def dummy_sql(self):
        sql = 'select * from imgs limit 10 offset 40355953'
        #sql = 'select count(*) from imgs'
        self.c.execute(sql)
        for row in self.c.execute(sql):
            print row


        pass
        #a = self.c.fetchone()
        #print 'Searching for %s - Found: %s' % (key, a)

    def read_source_file(self):
        self.create_sqlite()
        print '=== Reading source img file, will take a few minutes...'
        c = self.conn.cursor()
        print timestamp()
        t0 = time.time()
        t1 = t0
        count = 0
        sql_insert = 'INSERT INTO imgs VALUES ("%s",0)'
        for line in fileinput.input([IMG_FILE]):
            count += 1
            key = line.strip()[8:-4]
            c.execute(sql_insert % key)
            if time.time() > t1 + 20:
                self.conn.commit()
                total_elapsed_time = time.time() - t0
                print int(count/total_elapsed_time), count
                t1 = time.time()

        self.conn.commit()
        self.conn.close()
        print timestamp()
        print count

    def create_sqlite(self):
        print '=== Droping and creating imgs DB'
        c = self.conn.cursor()
        c.execute('DROP TABLE IF EXISTS imgs')
        c.execute('CREATE TABLE imgs (img text, is_used integer default 0)')
        c.execute('CREATE UNIQUE INDEX idx1 ON imgs(img)')


t = ThumbNailHandling()
t.match_edm_objs()
if 0:
    print timestamp()
    t.img_exists('FFFFECD16CBB912E0BB0D582479A2DE3871D9F5693A7779DCF52BD032904364F')
    print timestamp()
    t.img_exists('sune')
    print timestamp()
    t.img_exists('000003EFB0CFD8833ADFC10D7502D4C8F6B7E407EF58E13F4184E0CDE7A82357')
    print timestamp()
    t.img_exists('FD5472FC5588D043F36C9BA6E8CD5346ECAE93BD5961D291D6B1BFAC15E69951')
    print timestamp()
