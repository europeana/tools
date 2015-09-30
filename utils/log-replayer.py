#!/usr/bin/env python
"""Replay requests from a log file, subclass with handlers for the specific format.

- Takes time between requests into account, with option to speed up the replay.

  Inspired by https://github.com/chromano/apache-log-replay

  Written 2015-09-30 by jacob.lundqvist@europeana.eu
  License: EUPL
"""

import sys
import subprocess
import time
import requests
import datetime
import random
from optparse import OptionParser

try:
    from exceptions import StandardError
except:
    pass  # was py3


class LogReplayer(object):
    TIME_PROGRESS  = 5 # intervall for showing progress

    def __init__(self):
        """Parse command line options."""
        usage = "usage: %prog [options] logfile"
        parser = OptionParser(usage)
        parser.add_option('-s', '--speedup',
            help='make time run faster by factor SPEEDUP',
            dest='speedup',
            type='int',
            default=1)
        (self.options, args) = parser.parse_args()
        if len(args) == 1:
            self.fname = args[0]
        else:
            parser.error("incorrect number of arguments")

    def logfile_read(self):
        """returns timestamp + query param, one line at a time"""
        first_line = True
        ts_offset = 0
        with open(self.fname, 'r') as fh:
            for line in fh:
                #print ('logfile_read() processing line')
                ts, url = self.parse_logline(line[:-1])
                if first_line and ts:
                    ts_offset = ts
                    first_line = False
                yield ts - ts_offset, url

    def run(self):
        t_offset = t_progress = time.time()
        idx = 0
        for ts, url in self.logfile_read():
            if not url:
                continue # not usable line in the logfile
            idx += 1
            has_waited = False
            while (ts/self.options.speedup) > (time.time() - t_offset):
                 # wait until we are ready for next line
                 if not has_waited:
                     has_waited = True
                 time.sleep(0.001)
            self.handle_request(url)
            if t_progress + self.TIME_PROGRESS < time.time():
                print('progress: running %i secs at speed %i, sent %i requests' % (int(time.time() - t_offset), self.options.speedup, idx))
                t_progress = time.time()
        print('>>>>> logfile completed <<<<<')

    def forceprint(self, msg):
        sys.stdout.write(msg)
        sys.stdout.flush()

    def handle_request(self, url):
        """
        This basic method only curls the url not caring about success/fails if you want to process the output
        override this
        """
        if url.find('"') > -1:
            print('found double quote in url, that is not tested...')
        cmd = 'curl "%s" &' % url
        status = subprocess.call(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    # =============================================================================
    #
    # Override parse_logline()
    #
    # =============================================================================

    def parse_logline(self, line):
        """Override to handle your specific log format, should return a timestamp, and the finalised url
            ts, url
            if the line couldnt be parsed, return 0, None
        """
        ts = time.time()
        url = 'http://europeana.eu'
        return ts, url





""" =============================================================================

Handling our solr logfiles

"""
class SolrLogReplayer(LogReplayer):
    def parse_logline(self, line):
        #print('parse_logline: %s' % line)
        s, q = line.split('Solr query: q=')
        idx = random.randint(1,6)
        self.forceprint('%i<%s' % (idx,q))
        url = "http://sol%i.eanadev.org:9191/solr/search_1/select?q=%s" % (idx, q)
        s2,remainder = s.split('+')[0].split('.')
        dt = datetime.datetime.strptime(s2, "%Y-%m-%dT%H:%M:%S")
        ts = time.mktime(dt.timetuple())
        return ts, url





"""=============================================================================

  parsing portal2 Cloud Foundry app output, generated like:
    cf logs blue-portal > blue-portal.log

"""
class Portal2LogReplayer(LogReplayer):
    def parse_logline(self, line):
        try:
            if line.find('portal/search.html') > -1:
                url = self.extract_url(line)
            elif line.find('portal/record') > -1:
                url = self.extract_url(line)
            else:
                raise StandardError('neither query or record present')
        except:
            return 0, None # line could not be used
        t = line.split('.')[0]
        dt = datetime.datetime.strptime(t, "%Y-%m-%dT%H:%M:%S")
        ts = time.mktime(dt.timetuple())
        return ts, url

    def extract_url(self, line):
        parts = line.split('"referer":')
        if len(parts) > 2:
            raise StandardError('query, referer not found')
        try:
            url = parts[1][1:].split('","')[0]
        except:
            raise StandardError('query, failed to extract url')
        return url




if __name__ == "__main__":
     lr = Portal2LogReplayer()
     lr.run()
