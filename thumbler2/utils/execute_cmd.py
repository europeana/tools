"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)
"""

import subprocess
import time

try:
    from django.utils.encoding import smart_unicode
    USING_SMART_UNICODE=True
except:
    USING_SMART_UNICODE=False


CMD_TIMEOUT = 30
CMD_RETRY_MAX = 25
CMD_RETRY_RETCODE = 'retcode'
CMD_RETRY_STDERR = 'stderr'
CMD_RETRY_STDOUT = 'stdout'
CMD_RETRY_LIMIT = 'retry_limit'



class ExecuteCommand(object):

    def cmd_execute1(self, cmd, timeout=CMD_TIMEOUT):
        "Returns 0 on success, or error message on failure."
        result = 0
        retcode, stdout, stderr = self.cmd_execute_output(cmd, timeout)
        if retcode or stdout or stderr:
            result = u'retcode: %s' % retcode
            if stdout:
                result += u'\nstdout: %s' % stdout
            if stderr:
                result += u'\nstderr: %s' % stderr
        return result


    def cmd_execute_output(self, cmd, timeout=CMD_TIMEOUT, retry_cond = ()):
        """Returns retcode,stdout,stderr.
        retry_condition = {CMD_RETRY_RETCODE:int,
                           : 'string in stderr',
                           CMD_RETRY_STDOUT: 'string in stdout',
                           CMD_RETRY_LIMIT: CMD_RETRY_MAX}
            not all need to be given, retry_limit defaults to CMD_RETRY_LIMIT

            CMD_RETRY_RETCODE
            CMD_RETRU_STDERR
            CMD_RETRU_STDOUT
            CMD_RETRY_LIMIT
        """
        self.log('External command: [%s]' % cmd, 9)
        if not retry_cond:
            return self._do_execute_output(cmd, timeout)
        try_count = 0
        while True:
            try_count += 1
            retcode, stdout, stderr = self._do_execute_output(cmd, timeout)
            if (retcode == 0) and (not stderr):
                break # Success!

            if retry_cond.has_key(CMD_RETRY_RETCODE) and (retry_cond[CMD_RETRY_RETCODE] != retcode):
                break
            if retry_cond.has_key(CMD_RETRY_STDERR) and (stderr.find(retry_cond[CMD_RETRY_STDERR]) < 0):
                break
            if retry_cond.has_key(CMD_RETRY_STDOUT) and (stdout.find(retry_cond[CMD_RETRY_STDOUT]) < 0):
                break
            if retry_cond.has_key(CMD_RETRY_LIMIT):
                max_retries = retry_cond[CMD_RETRY_LIMIT]
            else:
                max_retries = CMD_RETRY_MAX
            if try_count >= max_retries:
                break
            self.log('=== cmd failed, will try again...', 9)
        return retcode, stdout, stderr


    def _do_execute_output(self, cmd, timeout):
        if isinstance(cmd, (list, tuple)):
            cmd = ' '.join(cmd)
        self._cmd_std_out = u''
        self._cmd_std_err = u''
        #try:
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
        if USING_SMART_UNICODE:
            stdout = smart_unicode(self._cmd_std_out, errors='replace')
            stderr = smart_unicode(self._cmd_std_err, errors='replace')
        else:
            stdout = self._cmd_std_out
            stderr = self._cmd_std_err

        #except:
        #    retcode = 1
        #    stdout = u''
        #    stderr = u'cmd_execute() exception - shouldnt normally happen'
        return retcode, stdout, stderr


    def _cmd_purge_io_buffers(self, p):
        try:
            s_out, s_err = p.communicate()
        except:
            return
        if s_out:
            self._cmd_std_out += s_out.decode('utf-8','ignore').encode('ascii','ignore')
        if s_err:
            self._cmd_std_err += s_err.decode('utf-8','ignore').encode('ascii','ignore')


try:
    from logit import LogIt

    class ExecuteCommandWithLog(ExecuteCommand, LogIt):
        """
        Dont forget this if you override __init__
        LogIt.__init__(self, self.debug_lvl, SIP_LOG_FILE, settings.PRINT_LOG)

        """
        pass
except:
    pass


