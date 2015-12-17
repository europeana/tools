

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

Iterate start in reasonable rows chunks until numFound records

"""



import subprocess
import sys
import time




CMD_TIMEOUT = 30
BATCH_SIZE = 100


class Foo(object):
    def __init__(self):
        if len(sys.argv) < 2:
            print 'Param should be host:port to be used'
            sys.exit(1)
        self.host = sys.argv[1]

    def run(self):
        self.getNumFound()
        self.retrieve_records()

    def retrieve_records(self):
        start = 0
        q = 'http://%s/solr/search/select?q=*%3A*&start=%i&rows=%i&fl=europeana_id&wt=csv' % (self.host, start BATCH_SIZE)
        result = self.cmd_execute_abort_on_error(cmd)

        return True


    def getNumFound(self):
        q = 'http://%s/solr/search/select?q=*%3A*&rows=0&fl=europeana_id&wt=xml&indent=true' % self.host
        result = self.cmd_execute_abort_on_error(cmd)
        parts = result.split('numFound="')
        self.numFound = long(parts[1].split('"')[0])

    def cmd_execute_abort_on_error(self, cmd, timeout=CMD_TIMEOUT):
        retcode, stdout, stderr = self.cmd_execute_output(cmd, timeout)
        if stderr:
            print
            print 'Errormsg:', stderr
            sys.exit(1)
        elif retcode:
            print
            print 'Errorstatus:', retcode
            sys.exit(1)
        return stdout

    def cmd_execute_output(self, cmd, timeout=CMD_TIMEOUT):
        "Returns retcode,stdout,stderr."
        if isinstance(cmd, (list, tuple)):
            cmd = ' '.join(cmd)
        self.log('External command: [%s]' % cmd, 3)
        self._cmd_std_out = u''
        self._cmd_std_err = u''
        try:
            t_fin = time.time() + timeout
            p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            while p.poll() == None and t_fin > time.time():
                self._cmd_purge_io_buffers(p)
                time.sleep(0.1)

            if p.poll() == None:
                stderr = u'Timeout for command %s' % cmd
                self.log(u'*** %s' % stderr, 1)
                self._cmd_purge_io_buffers(p)
                self.log(u'stdout: %s' % self._cmd_std_out, 1)
                self.log(u'stderr: %s' % self._cmd_std_err, 1)
                return 1,u'',stderr

            self._cmd_purge_io_buffers(p) # do last one to ensure we got everything
            retcode = p.returncode
            stdout = self._cmd_std_out
            stderr = self._cmd_std_err
        except:
            retcode = 1
            stdout = u''
            stderr = u'cmd_execute_output() exception - shouldnt normally happen'
        self.log(' exitcode: %i\n stdout: %s\n stderr: %s' % (retcode, stdout, stderr), 3)
        return retcode, stdout, stderr




Foo