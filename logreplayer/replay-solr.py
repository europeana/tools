#!/usr/bin/env python


import datetime
import time

from logreplaylib import LogReplayer



""" =============================================================================
Handling our solr logfiles

  Notice, this app can easilly kill backends, only ever run it vs servers you own and control,
  this traffic is easy to track and could be considered Denial of Service if directed to third party servers!!!


"""
class SolrLogReplayer(LogReplayer):
    def custom_options(self, parser):
        parser.add_option('-u', '--url',
            help='url to use for replaying (upto and including q=)',
            dest='url')

    def verify_options(self, parser):
        if not self.options.url:
            parser.error('You must specify -u')

    def parse_logline(self, line):
        try:
            s, q = line.split('Solr query: q=')
            url = self.options.url + q
            s2 = s.split('+')[0].split('.')[0]
            dt = datetime.datetime.strptime(s2, "%Y-%m-%dT%H:%M:%S")
            ts = time.mktime(dt.timetuple())
        except:
            # Failed to parse line
            ts = 0
            url = None
        return ts, url






if __name__ == "__main__":
     lr = SolrLogReplayer()
     lr.run()
