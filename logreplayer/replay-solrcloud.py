#!/usr/bin/env python

import random
import datetime
import time

from logreplaylib import LogReplayer



""" =============================================================================
Handling our solr logfiles

  Notice, this app can easilly kill backends, only ever run it vs servers you own and control,
  this traffic is easy to track and could be considered Denial of Service if directed to third party servers!!!


"""
class SolrCloudLogReplayer(LogReplayer):
    def parse_logline(self, line):
        try:
            s, q = line.split('Solr query: q=')
            idx = random.randint(1,6)
            url = "http://sol%i.eanadev.org:9191/solr/search_1/select?q=%s" % (idx, q)
            s2 = s.split('+')[0].split('.')[0]
            dt = datetime.datetime.strptime(s2, "%Y-%m-%dT%H:%M:%S")
            ts = time.mktime(dt.timetuple())
        except:
            # Failed to parse line
            ts = 0
            url = None
        return ts, url






if __name__ == "__main__":
     lr = SolrCloudLogReplayer()
     lr.run()
