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

  This files captures two types of loglines:
  a) search
        2015-10-02T13:35:11.30+0200 [App/1]      OUT 2015-10-02 11:35:11 INFO  ClickStreamJsonLogServiceImpl:154 - {"sessionId":"2D73EC1A9BABD5A9786164B3B9D38197","date":"2015-10-02T11:35:11.304Z","ip":"5.22.150.141","view":"/default/search/search","query":"gymnasium eutin","countryFacet":"germany (1)","lang":"en","req":"http://p.green.portal.europeana.eu:80/search.html?qt=false&query=gymnasium%20eutin&qf=&rows=10","user-agent":"Mozilla/4.0 (compatible; KVK/3.0.0; http://kvk.uni-karlsruhe.de)","v":"2.0","action":"BRIEF_RESULT","page":1,"numFound":1,"langFacet":"de (1)"}
    extracted:
        2015-10-02T13:35:11
        [url-param]/search.html?qt=false&query=gymnasium%20eutin&qf=&rows=10

  b) records
        2015-10-02T13:35:12.01+0200 [App/0]      ERR 2015-10-02 11:35:12 INFO  ClickStreamJsonLogServiceImpl:187 - {"sessionId":"AABEE415202DD270EBD19D9DC109BF02","date":"2015-10-02T11:35:12.010Z","ip":"5.22.150.141","referer":"https://www.google.es/","utma":"118166301.678394582.1438332662.1438332662.1441741868.2","lang":"en","req":"http://p.green.portal.europeana.eu:80/record/9200143/BibliographicResource_2000069299653.html","user-agent":"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36","v":"2.0","action":"FULL_RESULT_HMTL","europeana_uri":"/9200143/BibliographicResource_2000069299653"}
     extracted:
        2015-10-02T13:35:12
        [url-param]/record/9200143/BibliographicResource_2000069299653.html

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
