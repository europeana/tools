

"""

Step 1 get a xml format query to get numFound

http://sandbox41.isti.cnr.it:9191/solr/search/select?q=*%3A*&rows=0&fl=europeana_id&wt=xml&indent=true
<response>
<lst name="responseHeader">
<int name="status">0</int>
<int name="QTime">0</int>
<lst name="params">
<str name="fl">europeana_id</str>
<str name="indent">true</str>
<str name="q">*:*</str>
<str name="wt">xml</str>
<str name="rows">0</str>
</lst>
</lst>
<result name="response" numFound="42193613" start="0"></result>
</response>


http://sandbox41.isti.cnr.it:9191/solr/search/select?q=*%3A*&start=2000000&rows=10&fl=europeana_id&wt=csv

europeana_id
"/09003/DB36FBC852A844EB80BBD319B4C20ACBE245A7DB"
"/09003/C14CC2D77FA15D60B24D24C4CA842F38E8E9ADF1"
"/09003/E0983F1AE15CF13D4D6474730E70A9FAAE63BC36"
"/09003/F4D6CF5DA47B4964FE936C7154D719D2A7174436"
"/09003/F311AB06997F9D10F2F44BC5CC70052C567AF59E"
"/09003/CE725B9A2B9683A152E9002DFBB34CD8D02D063B"
"/09003/F56D65E32438EF5A93EA9F6B04F03E70A510DA46"
"/09003/57E1D4306174DDEF7C997B98CDF9B68439062788"
"/09003/7B901B857A3876ADDBFFE9B1627385220AF410F5"
"/09003/EC9EEBB2F0FCD3B45BF0B590B572FFF0D26A05DA"

Iterate start in reasonable rows chunks until numFound records

"""



import os
import sys
import tempfile
import time
import urllib2

import execute_cmd
import logit


FIELD='europeana_id'
#FIELD='provider_aggregation_edm_object'



BATCH_COUNT = 1000000
RESULT_SIZE = 4096
PROGRESS_INTERVALL = 10


class ETA(object):
    def __init__(self):
        self.etaReset()

    def etaReset(self, total_count=0, intervall=10):
        self.eta_current_idx = 0
        self._eta_old_count = 0
        self._eta_intervall = intervall
        self.etaSetTotalCount(total_count)
        self._eta_resume_start = 0
        self._eta_msg_template = 'remaining: %i since last update: %i \tpercent:%3.2f ETA: %s'

    def etaSetTotalCount(self, i):
        self._eta_total_count = i

    def etaStart(self, total_count=0, intervall=10):
        if total_count:
            self.etaReset(total_count, intervall)
        self._eta_time_start = time.time()
        self.etaCalculateNextProgress()

    def etaResumeStart(self, i_resume):
        self.eta_current_idx = i_resume
        self._eta_resume_start = time.time()
        self._eta_resume_percent = self.__etaCalculatePercent()

    def etaCalculateNextProgress(self):
        self._eta_next_progress = time.time() + self._eta_intervall

    def etaCalculate(self):
        percent_done = self.__etaCalculatePercent()
        now = time.time()
        if self._eta_resume_start:
            if time.time() > (self._eta_resume_start + 300):
                done_in_period_percent = percent_done - self._eta_resume_percent
                time_processing = now - self._eta_resume_start
                estimated_total_time = time_processing / done_in_period_percent
                time_since_start = estimated_total_time * percent_done
                self._eta_time_start = now - time_since_start
                eta = self.__etaCalculateEta(percent_done)
                self._eta_resume_start = 0
            else:
                eta = 0
        else:
            eta = self.__etaCalculateEta(percent_done)
        return percent_done, now, eta

    def etaProgress(self, check_timeout=False):
        if check_timeout and (time.time() < self._eta_next_progress):
            # its up to us to make sure we dont print progress to often..
            return
        percent_done, now, eta = self.etaCalculate()

        print self._eta_msg_template % (self._eta_total_count - self.eta_current_idx,
                                        self.eta_current_idx - self._eta_old_count,
                                        percent_done,
                                        time.asctime(time.localtime(eta)))
        self._eta_old_count = self.eta_current_idx
        if check_timeout:
            self.etaCalculateNextProgress()
        return

    def __etaCalculatePercent(self):
        return max(0.001, (float(self.eta_current_idx)/self._eta_total_count) * 100)

    def __etaCalculateEta(self, percent_done):
        return self._eta_time_start + ((time.time() - self._eta_time_start) / (percent_done / 100))



class Foo(ETA, execute_cmd.ExecuteCommand, logit.LogIt):
    def __init__(self):
        super(Foo, self).__init__()
        logit.LogIt.__init__(self,log_lvl=3)
        if len(sys.argv) < 3:
            print 'Params should be host:port records_file [resume]'
            sys.exit(1)
        self.host = sys.argv[1]
        self.records_file = sys.argv[2]
        if len(sys.argv) > 3:
            self.resume = sys.argv[3]
        else:
            self.resume = False

        self.url_base = 'http://%s/solr/search/select?' % self.host + 'q=*%3A*'
        self.numFound = 0

    def run(self):
        self.getNumFound()
        self.retrieve_records()

    def retrieve_records(self):
        self.etaStart(self.numFound, PROGRESS_INTERVALL)
        skipped_lines = 0
        if self.resume:
            try:
                skipped_lines = long(self.resume)
                self._eta_msg_template = 'remaining: %i since last update: %i \tpercent:%3.2f ETA: %s' + '\tskipped lines: %i' % skipped_lines
            except:
                pass
            cmd = 'wc -l %s' % self.records_file
            retcode,stdout,stderr = self.cmd_execute_output(cmd)
            if retcode:
                self.log('ERROR: Could not find file to resume', 0)
                sys.exit(1)
            start = int(stdout.split()[0]) + skipped_lines
            resume_start = True
            m = 'a'
            self.log('resuming from line %i' % start, 1)
        else:
            m = 'w'
            start = 0
            resume_start = False
        self.fp_out = open(self.records_file, mode=m)

        t0 = time.time() + self._eta_intervall
        while start < self.numFound:
            #print 'requesting items starting with', start
            url = '%s&start=%i&rows=%i&fl=%s&wt=csv' % (self.url_base, start, BATCH_COUNT, FIELD)
            sys.stdout.write('.') ; sys.stdout.flush()
            t1 = time.time()
            i_try = 1
            while i_try < 5:
                try:
                    req = urllib2.Request(url)
                    response = urllib2.urlopen(req, timeout=180)
                    break
                except:
                    self.log('WARNING: web server not responding', 0)
                    i_try += 1
            else:
                self.log('ERROR: to many errors, aborting', 0)
                sys.exit(1)
            if resume_start:
                # dont log the resume start until we actually start processing...
                self.etaResumeStart(start)
                resume_start = False

            sys.stdout.write('%i ' % int(time.time() - t1)) ; sys.stdout.flush()
            foo = response.readline() # get csv label line
            lines = response.readlines(RESULT_SIZE)
            while lines:
                for line in lines:
                    self.eta_current_idx += 1
                    record = line.strip()
                    if record[0] in ('"\''):
                        record = record[1:-1]
                    if record:
                        self.fp_out.write('%s\n' % record)
                    else:
                        skipped_lines += 1
                        self._eta_msg_template = 'remaining: %i since last update: %i \tpercent:%3.2f ETA: %s' + '\tskipped lines: %i' % skipped_lines
                    if time.time() > t0:
                        self.fp_out.flush()
                        self.etaProgress()
                        t0 = time.time() + self._eta_intervall
                lines = response.readlines(RESULT_SIZE)


            self.fp_out.flush()
            start += BATCH_COUNT

        self.fp_out.close()
        return True

    def getNumFound(self):
        url = '%s&rows=0&fl=europeana_id&wt=xml&indent=true' % self.url_base
        result = self.getWebRequest(url)
        parts = result.split('numFound="')
        self.numFound = long(parts[1].split('"')[0])

    def getWebRequest(self, url):
        req = urllib2.Request(url)
        response = urllib2.urlopen(req)
        html = response.read()
        return html




if __name__ == "__main__":
    Foo().run()
