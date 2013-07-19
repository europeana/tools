import os

from django.shortcuts import render_to_response, get_object_or_404
from django.conf import settings

from django.contrib.auth.decorators import login_required

import models
import sip_task



@login_required
def restart_dashboard(request):
    st = sip_task.SipTask()
    code,stdout,stderr = st.cmd_execute_output('/usr/local/bin/dashboard_restart',120)
    if code:
        result = stderr.replace('\n','<br>')
    else:
        result = stdout.replace('\n','<br>')
    return render_to_response("sipmanager/restart_dashboard.html", {'request': request,
                                                         'output': result})

def oldest_update(request):
    a = models.ProcessMonitoring.objects.all().order_by('-last_change')
    return render_to_response("sipmanager/show_procs.html",
                              {
                                  'request': request,
                                  'procs': procs,
                                  'system_load': '%0.2f' % os.getloadavg()[0],
                                  'max_load_new_tasks': settings.MAX_LOAD_NEW_TASKS[0],
                                  'max_allowed_load': settings.MAX_LOAD_RUNNING_TASKS[0],
                              })



def show_monitor(request):
    procs = models.ProcessMonitoring.objects.order_by('time_created')
    return render_to_response("sipmanager/show_procs.html",
                              {
                                  'request': request,
                                  'procs': procs,
                                  'system_load': '%0.2f' % os.getloadavg()[0],
                                  'max_load_new_tasks': settings.MAX_LOAD_NEW_TASKS[0],
                                  'max_allowed_load': settings.MAX_LOAD_RUNNING_TASKS[0],
                              })


