#!/usr/bin/env python

import random
import datetime
import time

try:
    from exceptions import StandardError
except:
    pass  # was py3


from logreplaylib import LogReplayer



""" =============================================================================
Handling our solr logfiles

  randomize each request to go to one of the solrcloud nodes to spread load.

  Notice, this app can easilly kill backends, only ever run it vs servers you own and control,
  this traffic is easy to track and could be considered Denial of Service if directed to third party servers!!!

"""
class SolrCloudLogReplayer(LogReplayer):
    def custom_options(self, parser):
        parser.add_option('-M', '--max_offset',
            help='max offset for queries, lines with higher ofset will be ignored',
            dest='max_offset', type='int', default=0)

    def parse_logline(self, line):
        s, q = line.split('Solr query: q=')
        self.filter_out_high_start_queries(q)
        idx = random.randint(1,6)
        url = "http://sol%i.eanadev.org:9191/solr/search_1/select?q=%s" % (idx, q)
        s2 = s.split('+')[0].split('.')[0]
        dt = datetime.datetime.strptime(s2, "%Y-%m-%dT%H:%M:%S")
        ts = time.mktime(dt.timetuple())
        return ts, url

    def filter_out_high_start_queries(self, q):
        if self.options.max_offset < 1:
            return # feature not used
        try:
            i = int(q.split('start=')[1].split('&')[0])
        except:
            raise StandardError('offset was not int - this shouldnt really happen')
        if i > self.options.max_offset:
            raise StandardError('offset to high, skip this line')
        return





if __name__ == "__main__":
     lr = SolrCloudLogReplayer()
     lr.run()
