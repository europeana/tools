

# Version 0.3.1

import os
import sys
import time



from objextr_local import *

try:
    s = obj_file
except:
    raise NameError('objextr_local.py must define:  obj_file')

try:
    hashed_file
except:
    raise NameError('objextr_local.py must define:  hashed_file')

try:
    sorted_file
except:
    raise NameError('objextr_local.py must define:  sorted_file')

try:
    existing_europeana_objs
except:
    raise NameError('objextr_local.py must define:  existing_europeana_objs')

try:
    missing_europeana_objs
except:
    raise NameError('objextr_local.py must define:  missing_europeana_objs')

try:
    img_cache_base
except:
    raise NameError('objextr_local.py must define:  img_cache_base')

try:
    refresh_time
except:
    raise NameError('objextr_local.py must define:  refresh_time')





obj_start_sequence = 'provider_aggregation_edm_object"><str>'
obj_termination_sequence = '</str></arr></doc>'
all_items = 26958571


                       
def calculate_hash(item):
    """
    When calculating the content hash for the record, the following is asumed:
      the lines are stripped for initial and trailing whitespaces,
      sorted alphabetically
      each line is separated by one \n character
      and finaly the <record> and </record> should be kept!
    """
    if isinstance(item, unicode):
        item = item.encode('utf-8')
    r_hash = hashlib.sha256(item).hexdigest().upper()
    return r_hash
    
bad_urls = (
    'text/html',
    'Inhaltsverzeichnis',
    'NO-FIRE',
    'NULL',
    'VDE-604',
    'X:MVB',
    'get_thumb.cgi?',
    'http://10933/'
)

bad_url_start = '/tER'

bad_obj_prefix = ' E'

def extract_objects():
    import hashlib
    from xml.sax.saxutils import unescape as unescapeXml

    all_items = 26958571
    f_in = open(obj_file)
    #f_in = open('/Users/jaclu/Downloads/some_objects.xml')
    f_out = open(hashed_file,'w')
    
    data_buffer = ''
    i = 0
    t0 = t1 = time.time() + refresh_time
    old_count = obj_count = 0
    f_out.write('hash<tab>org obj<tab>unescaped xml\n')
    while not f_in.closed:
        in_pos = f_in.tell()
        data_buffer += f_in.read(8192)
        if in_pos == f_in.tell():
            break
        while True:
            idx_start = data_buffer.find(obj_start_sequence)
            idx_stop = data_buffer.find(obj_termination_sequence,idx_start)
            if not ((idx_start) > -1 and (idx_stop > idx_start)):
                break
            org_url = data_buffer[idx_start + len(obj_start_sequence):idx_stop].strip()
            data_buffer = data_buffer[idx_stop:]
            if not org_url:
                sys.stdout.write('!')
                sys.stdout.flush()
                continue
            if (org_url in bad_urls) or (org_url[0] in bad_url_start):
                #print 'Bad URL: [%s]' % org_url
                continue
            if org_url.find('</str><str>') > -1:
                continue
            if org_url.find('\n') > -1:
                #print "Found LF: [%s]" % org_url
                continue
            obj_count += 1
            if 1: 
                edm_object = unescapeXml(org_url)
                h = calculate_hash(edm_object)
                hashed_url = '%s/%s/%s' % (h[:2],h[2:4],h)
                line = '%s.jpg\t%s\t%s\n' % (hashed_url, org_url, edm_object)
            else:
                line = '%s\n' % org_url
            f_out.write(line)
            if time.time() > t1:
                percent_done = (float(obj_count)/all_items) * 100
                now = time.time()
                eta = t0 + ((now - t0) / (percent_done / 100))
                print 'remaining: %i  since last update: %i  percent:%3.2f ETA: %s' % (all_items - obj_count, 
                                                                                       obj_count - old_count,
                                                                                       percent_done, 
                                                                                       time.asctime(time.localtime(eta)))
                old_count = obj_count
                t1 = time.time() + refresh_time
        
        pass


def match_imgs():
    t0 = t1 = time.time() + refresh_time
    current_dir = ''
    img_online = 0
    img_not_online = 0
    old_count = idx = 0
    f_objs = open(sorted_file)
    f_ok = open(existing_europeana_objs,'w')
    f_bad = open(missing_europeana_objs,'w')
    
    line = f_objs.readline() # initial line with field names
    line = f_objs.readline() # first real line
    while line: 
        try:
            fname, europeana_obj, uri = line[:-1].split('\t')
            if not fname:
                raise
        except:
            print 'Invalid line: [%s]' % line[:-1]
            line = f_objs.readline()
            continue
        idx += 1
        dir_name = fname[:5]
        rel_fname = fname[6:]
        if dir_name != current_dir:
            current_dir = dir_name
            file_list = os.listdir(os.path.join(img_cache_base,current_dir))
        out_line = '%s\n' % europeana_obj
        if rel_fname in file_list:
            img_online += 1
            f_ok.write(out_line)
        else:
            img_not_online += 1
            f_bad.write(out_line)
        if time.time() > t1:
            percent_done = (float(idx)/all_items) * 100
            now = time.time()
            eta = t0 + ((now - t0) / (percent_done / 100))
            print 'remaining: %i since last update: %i \tfound: %i missing: %i \tpercent:%3.2f ETA: %s' % (all_items - idx, 
                                                                                   idx - old_count,
                                                                                   img_online, 
                                                                                   img_not_online, 
                                                                                   percent_done, 
                                                                                   time.asctime(time.localtime(eta)))
            t1 = time.time() + refresh_time
            old_count = idx
            i = 0
        line = f_objs.readline()
    f_ok.close()
    print 'Found imgs (saved in %s): %i' (existing_europeana_objs, img_online)
    print 'Itm no img:', img_not_online
            
#extract_objects()
match_imgs()

"""
edm_object http://prensahistorica.mcu.es/es/catalogo_imagenes/imagen_id.cmd?idImagen=577421&amp;formato=jpg&amp;altoMaximo=200&amp;anchoMaximo=125
expected 5C/39/5C39A282B9362DB7D035175F61B358F5026C8ED6540D57A18334A9C86DA70FF7.jpg

calculate_url_hash 3B/CF/3BCFA0257E88D4B5F6FD98299D818042979E2FA180D38541B3E97DC2BEFC913A
"""