#!/usr/bin/env python

import datetime
import time


try:
    from exceptions import StandardError
except:
    pass  # was py3




from logreplaylib import LogReplayer



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
