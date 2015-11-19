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

Processing a rgular apache log, example log line
209.222.8.67 - - [11/Jul/2014:13:45:08 +0200] "GET /open-pics-app HTTP/1.1" 503 290

"""
class ApacheLogReplayer(LogReplayer):
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
        t = line.split('[')[1].split(' ')[0]  # 11/Jul/2014:13:35:42
        dt = datetime.datetime.strptime(t, "%d/%b/%Y:%H:%M:%S")
        ts = time.mktime(dt.timetuple())
        s = line.split('"GET ')[1].split(' ')[0]
        url = self.options.url + s
        return ts, url




if __name__ == "__main__":
     lr = ApacheLogReplayer()
     lr.run()
