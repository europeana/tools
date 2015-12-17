"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)

 Generic utility functions hep
"""

import hashlib
import subprocess
import urllib


def dict_2_django_choice(d):
    lst = []
    for key in d:
        lst.append((key, d[key]))
    return lst


def calculate_url_hash(url):
    try:
        s = url.encode('utf-8')
    except:
        s = url
    item = urllib.unquote_plus(s)
    return calculate_hash(item)
                       

def calculate_hash(item):
    """
    When calculating the content hash for the record, the following is asumed:
      the lines are stripped for initial and trailing whitespaces,
      sorted alphabetically
      each line is separated by one \n character'
      and finaly the <record> and </record> should be kept!
    """
    if isinstance(item, unicode):
        item = item.encode('utf-8')
    r_hash = hashlib.sha256(item).hexdigest().upper()
    return r_hash


def count_records(full_path):
    # How I hate this, for just a few files awk fails on a few files
    # try it again with the much slower grep
    rec_count = 0
    for cmd in ('cat \'%s\' | awk -F "<record>" \'{s+=(NF-1)} END {print s}\'' % full_path,
                'cat "%s" | grep "<record>" | wc -l' % full_path):
        try:
            if full_path[-3:].lower() == '.gz':
                cmd = 'z' + cmd
            s = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE).communicate()[0].strip() or 0
            rec_count = int(s)
        except:
            rec_count = 0
        if rec_count > 0:
            break
    return rec_count




def __db_find_type():
    from django.db import connection
    cursor = connection.cursor()
    if hasattr(cursor, 'db'):
        # if DEBUG=True its found here...
        look_at = cursor.db
    else:
        # Otherwise we find it here
        look_at = cursor
    db_s = look_at.__str__()
    if db_s.find('mysql') > -1:
        s = 'mysql'
    elif db_s.find('sqlite3') > -1:
        s = 'sqlite3'
    elif db_s.find('postgres') > -1:
        s = 'postgres'
    else:
        s = 'unknown'
    return s



if __name__ == "__main__":
    import sys
    if len(sys.argv) != 2:
        print 'Param should be the item that should be hashed'
        sys.exit(1)
    print calculate_hash(sys.argv[1])
else:
    db_type = __db_find_type()