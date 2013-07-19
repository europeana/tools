import codecs 
import os
import string

import simplelog


LF = '\n'


class XmlSplitter(object):
    def __init__(self, org_file_name, tmpdir, 
                 record_separator,  # xml tag or similar hinting at a new record ie suitable split point
                 records_per_file=2500,
                 part_wrapper = 'xmlsplitted xmlns:repox="http://repox.ist.utl.pt"', # wrapping tag for the part in the tempfiles
                                               # if opts are needed give them like this:
                                               #   'xmlsplitted xmlns:repox="http://repox.ist.utl.pt"'
                 log_lvl=7, # can also be a simplelog.SimpleLog object
                 log_file='', print_log=True):
        self.org_file_name = org_file_name
        
        self.tmpdir = tmpdir
        self.tag_record_start = '<%s' % record_separator
        self.tag_record_end = '</%s' % record_separator
        self.records_per_file = records_per_file
        self.part_tag_start = '<%s>\n' % part_wrapper
        self.part_tag_close = '</%s>\n' % part_wrapper.split()[0]
            
        self.part_wrapper = part_wrapper
        if isinstance(log_lvl, simplelog.SimpleLog):
            self.log_cls = log_lvl
        else:
            self.log_cls = simplelog.SimpleLog(log_lvl=log_lvl, log_file=log_file, print_log=print_log)
        self.log = self.log_cls.log
        
        self.part_open = False # a part file has been created and ready to be written to
        self.parts_count = 0  # current part file
        self.part_buff = [] # buffer for tempfile
        self.record_active = False # a record has been created and is active
        self.part_rec_count = 0  #  nr of recs in current part file
        
        
        
    def split_file(self,):
        self.log('=========    Splitting file into parts   =====', 1)
        for line in codecs.open(self.org_file_name, 'r', 'utf-8'):
            self.line_parser(line)
            
        self.part_flush(True)
        return self.parts_count
            
    def line_parser(self, line, recurse_lvl=0):
        if not self.record_active:
            parts = line.split(self.tag_record_start)
            if len(parts) > 2:
                self.log('found %i starting blocks in line (r %i)' % (len(parts)-1, recurse_lvl), 9)
            for part in parts[1:]:
                opts, filtered_content = self.line_filter(part)
                self.record_open('%s %s' % (self.tag_record_start, opts))
                self.line_parser(filtered_content,recurse_lvl)
                line = '' # avoid final reprocessing
        else:
            if line == '</repox:record>\n':
                pass
            parts = line.split(self.tag_record_end)
            if len(parts) > 1:
                if len(parts) > 2:
                    self.log('found %i ending blocks in line (r %i)' % (len(parts)-1, recurse_lvl), 9)
                opts, filtered_content = self.line_filter(parts[0])
                self.line_parser(filtered_content,recurse_lvl)
                self.record_close(opts)
                for part in parts[1]:
                    opts, filtered_content = self.line_filter(part)
                    self.line_parser(filtered_content,recurse_lvl)
        if self.record_active:
            self.record_append(line)
        
    def record_open(self, opts=''):
        "New record is starting"
        self.part_create()
        self.record_active = True
        self.part_rec_count += 1
        self.log('creating record %i' % self.part_rec_count, 7)
        self.record_append('%s>' % opts)
    
    def record_close(self, opts=''):
        "record is completed"
        self.record_append('%s %s>\n' % (self.tag_record_end, opts))
        self.log(' << closing record %i' % self.part_rec_count, 7)
        self.part_flush(False)
        self.record_active = False
    
    def record_append(self, line): #record_append
        "Add record to partial file"
        if not self.record_active:
            self.fail_abort('no record open for append', raise_it=True)
        if not line:
            return
        if not line[-1] == LF:
            line += LF
        self.part_buff.append(line)
            
    def part_create(self):
        "Create a new partial file"
        if self.part_open:
            return # recod is already open
        
        self.part_buff = []

        self.parts_count += 1            
        self.log('creating part %i' % self.parts_count, 2)
        fname_part = self.get_part_fname(self.parts_count)
        self.h_fname_part = codecs.open(fname_part, 'w', 'utf-8')
        self.h_fname_part.write('<?xml version="1.0" encoding="UTF-8"?>\n')
        self.h_fname_part.write(self.part_tag_start)
        self.part_open = True
        self.part_rec_count = 0
            
    def part_close(self):
        "Closes a part if it was open."
        if not self.part_open:
            return # asked to close if something was open
        
        self.log(' <  closing part %i' % self.parts_count, 5)        
        self.h_fname_part.writelines(self.part_buff)
        self.h_fname_part.write(self.part_tag_close)
        self.h_fname_part.close()
        self.part_open = False
    
    def part_flush(self, force=False):
        "if max records or force close edm file and reset per file rec count."
        if (self.part_rec_count < self.records_per_file) and (not force):
            return
        self.part_close()
        
    def line_filter(self, line):
        if not line or line == '>':
            return '',''
        elif line[0] == '<':
            return '', string.strip(line)
        i = line.find('>')
        return string.strip(line[:i]), string.strip(line[i+1:])
    
    def fail_abort(self, msg, raise_it=False):
        self.log('\n**************************************************\n%s' % msg, 1)
        if raise_it:
            raise
        sys.exit(1)

    def get_part_fname(self, idx):
        fname = os.path.join(self.tmpdir, 'xmlsplit-part-%i.xml' % idx)
        return fname
    
    #def ese_tmp_fname(self, idx):
    #    fname = os.path.join(self.tmp_dir, 'ese-part-%i.xml' % idx)
    #    return fname
    
        

        
            
