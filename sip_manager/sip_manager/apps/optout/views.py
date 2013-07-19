# Create your views here.

from django.shortcuts import render_to_response, redirect
from django.contrib.auth.decorators import login_required

from utils.execute_cmd import ExecuteCommandWithLog

@login_required
def top_index(request):
    ec = ExecuteCommandWithLog()
    retcode, optout_source, stderr = ec.cmd_execute_output('ssh -q optoutlst@sandbox13.isti.cnr.it')
    retcode, optout_test, stderr = ec.cmd_execute_output('ssh euadmin@test.europeana.eu cat ~tomcat/conf/opt-out.list.txt')
    retcode, optout_acceptance, stderr = ec.cmd_execute_output('ssh euadmin@acceptance.europeana.eu cat /usr/local/tomcat/conf/opt-out.list.txt')
    retcode, optout_production, stderr = ec.cmd_execute_output('ssh euadmin@portal1.europeana.sara.nl cat /usr/local/tomcat/conf/opt-out.list.txt')
    return render_to_response("optout/optout-index.html",
                              {
                                  'optout_source': optout_source,
                                  'optout_test' : optout_test,
                                  'optout_acceptance': optout_acceptance,
                                  'optout_production': optout_production,
                                   },)



@login_required
def sync_test(request):
    ec = ExecuteCommandWithLog()
    
    cmd = 'ssh tomcat@test.europeana.eu bin/sync-optout'
    retcode, stdout, stderr = ec.cmd_execute_output(cmd)
    if retcode:
        return cmd_failed(cmd, retcode, stdout, stderr)
    
    cmd = 'ssh -t -t euadmin@test.europeana.eu bin/tomcat_restart'
    retcode, stdout, stderr = ec.cmd_execute_output(cmd)
    if retcode:
        return cmd_failed(cmd, retcode, stdout, stderr)
    
    return redirect('optout_index')
    
@login_required
def sync_acceptance(request):
    extra_output = ''
    ec = ExecuteCommandWithLog()
    
    cmd = 'ssh tomcat@acceptance.europeana.eu bin/sync-optout'
    retcode, stdout, stderr = ec.cmd_execute_output(cmd)
    if retcode:
        return cmd_failed(cmd, retcode, stdout, stderr)
    cmd = 'ssh -t -t euadmin@acceptance.europeana.eu bin/tomcat_restart'
    retcode, stdout, stderr = ec.cmd_execute_output(cmd)
    if retcode:
        return cmd_failed(cmd, retcode, stdout, stderr)
        
    return redirect('optout_index')
    

def cmd_failed(cmd, retcode, stdout, stderr):
    return render_to_response("optout/errormsg.html",
                              {
                                  'cmd': cmd,
                                  'retcode': retcode,
                                  'stdout' : stdout,
                                  'stderr': stderr,
                                   },)
    