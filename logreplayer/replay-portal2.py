#!/usr/bin/env python

import datetime
import time
import urllib
import sys

try:
    from exceptions import StandardError
except:
    pass  # was py3




from logreplaylib import LogReplayer



"""=============================================================================
  parsing portal2 Cloud Foundry app output, generated like:
    cf logs blue-portal > blue-portal.log

  Notice, this app can easilly kill backends, only ever run it vs servers you own and control,
  this traffic is easy to track and could be considered Denial of Service if directed to third party servers!!!

"""
class Portal2LogReplayer(LogReplayer):
    def custom_options(self, parser):
        parser.add_option('-u', '--url',
            help='url for portal (including potential /portal suffix)',
            dest='url')

    def verify_options(self, parser):
        if not self.options.url:
            parser.error('You must specify -u')
        if self.options.url[-1] == '/':
            parser.error('url param can not end with a "/"')

    def parse_logline(self, line):
        try:
            if line.find('"action":"BRIEF_RESULT"') > -1:
                s = self.extract_url(line)
                s2 =s.split('search.html?')[1]
                url = '%s/search.html?%s' % (self.options.url, s2)
            elif line.find('"action":"FULL_RESULT_HMTL"') > -1:
                s = self.extract_url(line)
                s2 = s.split('/record/')[1]
                url = '%s/record/%s' % (self.options.url, s2)
            else:
                raise StandardError('neither query or record present')
        except:
            return 0, None # line could not be used
        t = line.split('.')[0]
        dt = datetime.datetime.strptime(t, "%Y-%m-%dT%H:%M:%S")
        ts = time.mktime(dt.timetuple())
        return ts, url

    def extract_url(self, line):
        parts = line.split('"req":')
        if len(parts) > 2:
            raise StandardError('query, req not found')
        try:
            s = parts[1][1:].split('","')[0]
            if sys.version_info >= (3, 0):
                url = urllib.parse.unquote(s)
            else:
                url=urllib.unquote(s).decode('utf8')
        except:
            raise StandardError('query, failed to extract url')
        return url






if __name__ == "__main__":
     lr = Portal2LogReplayer()
     lr.run()
