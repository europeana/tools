#!/usr/bin/env python


import datetime
import time

from logreplaylib import LogReplayer



""" =============================================================================
Handling our solr logfiles
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
            url = 'http://www.europeana.eu/portal/record/07602/6B7305AB23BBB256A2C78012DD300510A120CBAF.html'
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
