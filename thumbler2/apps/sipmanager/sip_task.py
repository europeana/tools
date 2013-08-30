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

import os
import random
import threading
import time

from django.conf import settings
from django.core.mail import send_mail


from utils.logit import LogIt
from utils.execute_cmd import ExecuteCommand

DJANGO_DEBUG = settings.DEBUG

try:
    from django import db
    import models
except:
    print '*** seems django is not installed, limited functionality...'
    
    import dummy_models as models
    DJANGO_DEBUG = False
    



# Since looking up in settings takes some extra cpu, important settings
# are cached within the module
TASK_PROGRESS_INTERVALL = settings.TASK_PROGRESS_INTERVALL
SIP_LOG_FILE = settings.SIP_LOG_FILE
MAX_LOAD_NEW_TASKS = settings.MAX_LOAD_NEW_TASKS
MAX_LOAD_RUNNING_TASKS = settings.MAX_LOAD_RUNNING_TASKS
THREAD_LIMIT = settings.THREAD_LIMIT

SHOW_DATE_LIMIT = 60 * 60 * 20 # etas further than this will display date


RUNNING_EXECUTORS = []
RUNNING_PLUGINS = {}

PLUGINS_MAY_NOT_RUN = False


class SipTaskException(Exception):
    """
    Dont forget:
    from django.conf import settings
    from django.core.mail import send_mail
    """
    def __init__(self, msg='', *args, **kwargs):
        self.msg = msg
        for eadr in settings.ADMIN_EMAILS:
            if eadr:
                send_mail('Thumblr2 SipTaskException', self.msg, '', [eadr], fail_silently=False)
    
    def __str__(self):
        return repr(self.msg)
    

class SipSystemOverLoaded(Exception):
    pass



# Last time a task was terminated
LAST_TASK_TERMINATION = 0

# we only try to kill tasks this often (seconds)
KILL_INTERVALL = 30

# Run mode for a task
SIPT_THREADABLE = 'threadale' # Can be run in multiple instances
SIPT_SINGLE = 'one thread' # one instance can be run whilst the monitor continues
SIPT_NOT = 'not threadable' # no threading, monitor should wait for this to complete

SIP_THREAD_STYLES = (SIPT_NOT, SIPT_SINGLE, SIPT_THREADABLE)

SIP_PRIO_LOW = 8
SIP_PRIO_NORMAL = 5
SIP_PRIO_INCREASED = 4
SIP_PRIO_HIGH = 2

SIP_PRIOS = (SIP_PRIO_LOW, SIP_PRIO_NORMAL, SIP_PRIO_INCREASED, SIP_PRIO_HIGH)


IS_TERMINATING = False # Flag from environment that we should terminate asap


NODE_NAME = settings.NODE_NAME


_sub_pid = 0

def get_subpid():
    global _sub_pid
    _sub_pid += 1
    return _sub_pid



class SipTask(LogIt, ExecuteCommand): #SipProcess(object):
    """
    This is the baseclass for sip Tasks

    each subclass should define a run() that does the actual work

    all locking to the database are done by this baseclass
    """
    SHORT_DESCRIPTION = '' # a one-two word description.

    INIT_PLUGIN = False  # If True, is run (once) before normal plugins

    THREAD_MODE = SIPT_NOT # Indicates if ths task can be run in multiple
                           # instances or not
    # For loadbalancing, set to True if this plugin uses a lot of system resources
    # taskmanager will try to spread load depending on what is indicated here
    PLUGIN_TAXES_CPU = False
    PLUGIN_TAXES_DISK_IO = False
    PLUGIN_TAXES_NET_IO = False

    # how often task status should be updated
    TASK_PROGRESS_TIME = TASK_PROGRESS_INTERVALL

    # tasks are sorted in priority order
    PRIORITY = SIP_PRIO_NORMAL

    # If instances is a positive nr only this many instances may run
    # mostly relevant for multithreaded plugins that dont accept to be disabled
    # on high load - we dont want to many such beasts running,
    # if unlimited they will eventually steal the system and bring it down ;)
    INSTANCES = 0



    def __init__(self, debug_lvl=-9, run_once=False):
        if debug_lvl == -9:
            debug_lvl = settings.SIPMANAGER_DBG_LVL
        self.debug_lvl = debug_lvl
        
        #super(UriManager, self).__init__(*args, **kwargs)
        LogIt.__init__(self, self.debug_lvl, SIP_LOG_FILE, settings.PRINT_LOG)

        self.skip_time_stamp = False
        if self.debug_lvl > 8:
            self.log('%s.__init__()' % self.__class__.__name__)

        if self.THREAD_MODE not in SIP_THREAD_STYLES:
            raise SipTaskException('Invalid THREAD_MODE')
        if self.PRIORITY not in SIP_PRIOS:
            raise SipTaskException('Invalid PRIORITY')

        self.run_once = run_once # if true plugin should exit after one runthrough
        self.pid = float('%i.%i' % (os.getpid(), get_subpid()))
        self.runs_in_thread = False
        self.is_prepared = False
        self.task_show_log_lvl = 5 # normal level for showing task progress
        self.initial_message = '' # if set during prepare() will be used for first progress
        self._task_show_time = time.time()
        self._task_steps = 0

    def run(self, *args, **kwargs):
        global RUNNING_EXECUTORS

        ret = False

        if PLUGINS_MAY_NOT_RUN:
            self.log('>> Plugin %s prevented from running due to PLUGINS_MAY_NOT_RUN' % self.__class__.__name__, 7)
            return ret
        
        if (THREAD_LIMIT > 0) and (threading.active_count() > THREAD_LIMIT):
            self.log('>> Plugin %s prevented from running due to THREAD_LIMIT reached' % self.__class__.__name__, 8)
            return ret

        if (self.THREAD_MODE == SIPT_SINGLE) and (self.short_name() in RUNNING_EXECUTORS):
            self.log('>> Plugin %s prevented from running due to only one instance allowed' % self.__class__.__name__, 8)
            return ret

        if self.instance_limit_reached():
            self.log('>> Plugin %s prevented from running due to instance_limit reached' % self.__class__.__name__, 8)
            return ret
        
        if self.do_prepare(*args, **kwargs):
            try:
                if self.THREAD_MODE in (SIPT_SINGLE, SIPT_THREADABLE):
                    self.log('%s.run_in_thread()' % self.__class__.__name__, 7)
                    ret = self.run_in_thread(self.run_it, *args, **kwargs)
                else:
                    self.log('%s.run_it()' % self.__class__.__name__, 7)
                    ret = self.run_it(*args, **kwargs)
            except SipSystemOverLoaded:
                ret = False

        if not self.runs_in_thread and self.is_prepared:
            self.process_cleanup()
        return ret


    def do_prepare(self, *args, **kwargs):
        self.log('%s.prepare()' % self.__class__.__name__, 8)
        self.is_prepared = False
        b = self.prepare(*args, **kwargs)
        if b:
            self.post_prepare()
        return b

    def post_prepare(self):
        "Do this once prepare has indicated something to be done."
        global RUNNING_PLUGINS
        
        self.log('Initializing task    +++++   %s   +++++' % self.short_name(), 8)
        self.pm = models.ProcessMonitoring(pid=('%s %s' % (NODE_NAME, self.pid)),
                                           plugin_module = self.__class__.__module__,
                                           plugin_name = self.__class__.__name__,
                                           #task_label=self.SHORT_DESCRIPTION
                                           )
        self.pm.save()
        if RUNNING_PLUGINS.has_key(self.pm.plugin_name):
            RUNNING_PLUGINS[self.pm.plugin_name].append(self.pid)
        else:
            RUNNING_PLUGINS[self.pm.plugin_name] = [self.pid]
        if settings.THREADING_PLUGINS and self.THREAD_MODE == SIPT_SINGLE:
            RUNNING_EXECUTORS.append(self.short_name())
            self.log('++++++ Starting executor %s - %i' % (self.short_name(), self.pm.pk), 8)
        self.task_starting(self.short_descr(), display=False)
        self.log('++ Starting task: %s %s - %i' % (self.short_name(),
                                                   self.initial_message,
                                                   self.pm.pk),
                 self.task_show_log_lvl + 1)
        self.is_prepared = True

    def _log_task_exception_in_monitor(self, inst):
        "TODO: If this task is in the process monitor, log the failure"
        return False


    def error_log(self, msg):
        print '*** %s %s' % (self.short_name(), msg)


    def abort_process(self, msg):
        "Terminats process, trying to clean up and remove all pid locks."
        self.log('*********************')

        pms = models.ProcessMonitoring.objects.filter(pid=self.pid)
        for pm in pms:
            # TODO: a process failed, flag it, and remove its lock
            pass
        raise SipTaskException(msg)


    def process_cleanup(self):
        global RUNNING_EXECUTORS
        global RUNNING_PLUGINS

        self.log('-- Finishing task: %s %s - %i' % (self.short_name(),
                                                    self.initial_message,
                                                    self.pm.pk),
                 self.task_show_log_lvl + 1)

        RUNNING_PLUGINS[self.pm.plugin_name].remove(self.pid)
        if not RUNNING_PLUGINS[self.pm.plugin_name]:
            RUNNING_PLUGINS.pop(self.pm.plugin_name)
            
        pm_id = self.pm.pk
        self.pm.delete()

        if self.runs_in_thread and (self.short_name() in RUNNING_EXECUTORS):
            # the runs_in_thread check is needed, to avoid removing an actual
            # running executor if we are terminating due to this executor
            # already running.
            # a running executor will set runs_in_thread True
            self.log('------- terminating executor %s - %i' % (self.short_name(), pm_id), 8)
            RUNNING_EXECUTORS.remove(self.short_name())

        # Theese clean up cached queries if settings.DEBUG = True
        # otherwise does nothing
        if DJANGO_DEBUG:
            db.reset_queries()
            db.connection.close()
        self.log('  Finished task  -----   %s' % self.short_name(), 8)


    def system_is_occupied(self, check_to_start_new_task=True):
        "dont start new tasks when load is high."
        r1 = r5 = r15 = False
        if self.PRIORITY == SIP_PRIO_HIGH:
            return False, (r1, r5, r15) # never report occupied to high prio tasks
        if check_to_start_new_task:
            limit1, limit5, limit15 = MAX_LOAD_NEW_TASKS
        else:
            limit1, limit5, limit15 = MAX_LOAD_RUNNING_TASKS
        load_1, load_5, load_15 = os.getloadavg()
        if load_1 >= limit1:
            r1 = True
        if load_5 >= limit5:
            r5 = True
        if load_15 >= limit15:
            r15 = True

        if r1 or r5 or r15:
            if not check_to_start_new_task:
                self.log('  load too high: %0.2f %0.2f %0.2f' % (load_1, load_5, load_15), 8)
            busy = True
        else:
            busy = False
        return busy, (r1, r5, r15)


    # ==========   Must be overloaded   ====================

    def run_it(self):
        msg = 'run_it() must be implemented!'
        print '******', msg
        raise SipTaskException(msg)


    # ==========   Can be overloaded   ====================

    def prepare(self):
        "This is called before run_it() if returns True, it indicates run_it will have something to do."
        return True


    # ==========   Thread handling   ====================

    def run_in_thread(self, mthd, *args, **kwargs):
        "If threading is disabled, mthd will be run normally."
        if settings.THREADING_PLUGINS:
            self.runs_in_thread = True
            args = (mthd,) + args
            t = threading.Thread(target=self.thread_wrapper,
                                 name='pmid-%i' % self.pm.pk,
                                 args=args, kwargs=kwargs)
            if self.debug_lvl > 8:
                self.log('%s - Starting thread: %s' % (self.__class__.__name__, t.name))
            t.start()
            return True
        else:
            return mthd(*args, **kwargs)


    def thread_wrapper(self, *args, **kwargs):
        global IS_TERMINATING
        
        mthd = args[0]
        args = args[1:]
        #try:
        mthd(*args, **kwargs)
        #except SipSystemOverLoaded:
            #pass
        #except:
            #print time.asctime() # give exception a timestamp
            #for msg in ('*****   PLUGIN CRACHED %s   *****' % self.__class__.__name__,
                        #'*****   Running Threads: %i   *****' % threading.active_count(),
                        #):
                #print msg
                #self.log(msg, -1, mail_it=True)
                #raise
            #IS_TERMINATING = True
        #try:
        self.process_cleanup()
        #except SipSystemOverLoaded:
            #pass
        #except:
            #print time.asctime() # give exception a timestamp
            #for msg in ('*****   PLUGIN CRACHED on cleanup %s   *****' % self.__class__.__name__,
                        #'*****   Running Threads: %i   *****' % threading.active_count(),
                        #):
                #print msg
                #self.log(msg, -1, mail_it=True)
            #IS_TERMINATING = True
        return


    def instance_limit_reached(self):
        if self.INSTANCES < 1:
            return False # we dont limit instances
        i = models.ProcessMonitoring.objects.filter(plugin_name=self.short_name()).count()
        if i >= self.INSTANCES:
            return True # instance limit reached
        return False

    # ==========   Pid locking mechanisms   ====================
    def grab_item(self, cls, pk, task_description='', wait=0):
        """Locks item to current pid, if successfull, returns updated item,
        otherwise returns None. Once the item is locked, nobody else but
        the locking process may modify it.

        if wait is given, this method will try to lock the item for that amount of time.

        It is recomended to use the returned object, instead of a possible earlier incarnation of it"""
        t0 = time.time()
        while True:
            try:
                item = cls.objects.filter(pk=pk)[0]
            except:
                item = None
            if item or (t0 + wait < time.time()):
                break # timeout!

        if item and (not item.pid):
            item.pid = self.pid
            item.save()
            if task_description:
                self.pm.task_label=task_description
                self.pm.save()
            return item
        else:
            # item exists but is already taken
            return None


    def release_item(self, cls, pk):
        item = cls.objects.filter(pk=pk)[0]
        if not item.pid == self.pid:
            return False
        item.pid = 0
        item.save()
        return True
    # ==========   End of Pid locking mechanisms   ====================


    # ==========   Task steps   ====================

    def task_starting(self, label, steps=0, display=True):
        "new subtask starting, give at label and if possible no of steps."
        self._task_time_start = time.time()
        self._task_steps = steps
        self._task_previous = 0

        if label:
            self.pm.task_label = label
        self.pm.task_progress = ''
        self.pm.task_eta = ''
        self.pm.save()
        if display:
            self.task_progress(0)
        self._task_show_time = time.time()
        
    def task_set_label(self, label):
        self.pm.task_label = label
        self.pm.save()

    def task_set_nr_of_steps(self, steps):
        self._task_steps = steps
        
    def task_force_progress_timeout(self):
        "Ensures that next call to task_time_to_show() will trigger."
        self._task_show_time = 0


    def task_time_to_show(self, progress='', terminate_on_high_load=False):
        """Either use as a bool check, or give a param directly.

        A number param is sent to task_progess()
        a string param is used directly.

        remember if terminate_on_high_load=True a SipSystemOverLoaded exception
        will be triggered on high load, you need to catch that in you code
        if you need to do any cleanup
        """
        if IS_TERMINATING:
            msg = 'System is terminating'
        elif PLUGINS_MAY_NOT_RUN and self.PRIORITY != SIP_PRIO_HIGH:
            msg = 'Plugin is ordered to terminate'
        else:
            msg = ''
        if msg:
            self.log('+++ %s %s' % (self.__class__.__name__, msg))
            raise SipSystemOverLoaded(msg)

        if self._task_show_time + self.TASK_PROGRESS_TIME < time.time():
            if progress:
                if isinstance(progress, int):
                    self.task_progress(progress)
                else:
                    self.pm.task_progress = progress
                    self.pm.save()
                    self.log('%s - %s | %i' % (self.pm.task_label,
                                               self.pm.task_progress,
                                               self.pm.id), 7)
            self._task_show_time = time.time()
            b = True
            if terminate_on_high_load:
                self.do_terminate_on_high_load()
        else:
            b = False
        return b


    def do_terminate_on_high_load(self):
        global LAST_TASK_TERMINATION

        busy, loads = self.system_is_occupied(check_to_start_new_task=False)
        if not busy:
            return

        if (LAST_TASK_TERMINATION + KILL_INTERVALL) > time.time():
            # only try to kill if not KILL_INTERVALL seconds have passed since last kill
            return 

        # It wouldnt make sense to terminate all processes
        # instead do a randomiztion and a kill percentage
        # also we leave the last task running until we hit the load15
        # ceiling
        task_count = models.ProcessMonitoring.objects.count()
        load_1, load_5, load_15 = loads
        msg = 'Terminating task %s due to high load' % self.pm.pk
        if load_15:
            # at this level allways terminate
            self.log('== %s 15' % msg, 2)
            LAST_TASK_TERMINATION = time.time()
            raise SipSystemOverLoaded('%s 15' % msg)
        elif load_5:
            # 50% propab
            if (task_count > 1) and (random.randint(1,10) > 5):
                self.log('== %s 5' % msg, 2)
                LAST_TASK_TERMINATION = time.time()
                raise SipSystemOverLoaded('%s 5' % msg)
        elif load_1:
            # 3 * task_count % , max 20 propab
            if (task_count > 1) and (random.randint(1,100) <= min(20,(3 * task_count))):
                self.log('== %s 1' % msg, 2)
                LAST_TASK_TERMINATION = time.time()
                raise SipSystemOverLoaded('%s 1' % msg)      

    def task_progress(self, step):
        "update stepcount and eta (from last call to task_starting()."
        if self._task_steps and step: # avoid zero div
            perc_done, self.pm.task_eta = self._task_calc_eta(step)
            since_last = step - self._task_previous
            self._task_previous = step
            self.pm.task_progress = '%i/%i %i  (%0.2f%%)' % (step, self._task_steps,
                                                             since_last, perc_done)
        else:
            self.pm.task_progress = '%i' % step
            self.pm.task_eta = 'unknown'
        self.pm.save()
        self.log('%s  -  %s  eta: %s | %i %s' % (self.pm.task_label, self.pm.task_progress,
                                              self.pm.task_eta, self.pm.id, PLUGINS_MAY_NOT_RUN),
                 self.task_show_log_lvl)


    def _task_calc_eta(self, step):
        percent_done = float(step) / self._task_steps * 100
        elapsed_time = time.time() - self._task_time_start
        eta_t_from_now = int(elapsed_time / ((percent_done / 100) or 0.001))
        eta = self._task_time_start + eta_t_from_now
        if (eta - time.time()) < SHOW_DATE_LIMIT:
            eta_s = time.strftime('%H:%M:%S', time.localtime(eta))
        else:
            eta_s = time.strftime('%m-%d %H:%M:%S', time.localtime(eta))
        return percent_done, eta_s

    # ==========   End of Task steps   ====================


    def send_email(self, recipient, subj, body):
        if recipient:
            send_mail(subj, body, '', [recipient], fail_silently=False)
        else:
            self.log('>> if mailadr was defined, this would have been sent:', 1)
            self.log('>> [%s] %s' % (subj, body), 1)



    def __unicode__(self):
        return '%s - [%s] %s' % (self.aggregator_id, self.name_code, self.name)

    def short_name(self):
        "Short oneword version of process name."
        # find name of this (sub-) class
        return self.__class__.__name__

    def short_descr(self):
        if not self.SHORT_DESCRIPTION:
            raise NotImplemented('SHORT_DESCRIPTION must be specified in subclass')
        return self.SHORT_DESCRIPTION

