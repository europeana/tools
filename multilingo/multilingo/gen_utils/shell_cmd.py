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

 Initial release: 2010-02-05
 Version 1.1 2010-06-09
"""


import subprocess



def cmd_execute(cmd, cwd='.'):
    "Returns 0 on success, or error message on failure."
    result = 0
    retcode, stdout, stderr = cmd_execute_output(cmd, cwd)
    if retcode:
        result = 'retcode: %s' % retcode
        if stdout:
            result += '\nstdout: %s' % stdout
        if stderr:
            result += '\nstderr: %s' % stderr
    return result




def cmd_execute_output(cmd, cwd='.'):
    "Returns retcode,stdout,stderr."
    if isinstance(cmd, (list, tuple)):
        cmd = ' '.join(cmd)
    try:
        p = subprocess.Popen(cmd, shell=True, cwd=cwd,
                             stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = p.communicate()
        retcode = p.returncode
    except:
        retcode = 1
        stdout = ''
        stderr = 'cmd_execute() exception - shouldnt normally happen'
    return retcode, stdout, stderr

