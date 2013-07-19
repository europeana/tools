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


# PMS_ = ProcssMonitoring status
PMS_RUNNING = 1
PMS_DISAPPEARED = 2
PMS_TERMINATED = 3

PMS_STATES = {
    PMS_RUNNING: 'running',
    PMS_DISAPPEARED: 'disappeared',
    PMS_TERMINATED: 'terminated',
    }

class ProcessMonitoring(object):
    #pid = models.CharField(max_length=200) # what process 'owns' this item
    #plugin_module = models.CharField(max_length=100)
    #plugin_name = models.CharField(max_length=100)
    #task_label = models.CharField(max_length=200)
    #task_progress = models.CharField(max_length=250, default='') # count and percentage done
    #task_eta = models.CharField(max_length=15, default='unknown')
    #status = models.IntegerField(choices=dict_2_django_choice(PMS_STATES),
    #                             default = PMS_RUNNING)
    #time_created = models.DateTimeField(auto_now_add=True, editable=False)
    #last_change =  models.DateTimeField(auto_now=True, editable=False)
    def __init__(self, pid, plugin_module, plugin_name):
        self.objects = []
        self.plugin_module = plugin_module
        self.plugin_name = plugin_name
        self.pk = 1
        self.id = 1

    def save(self):
        pass

    def delete(self):
        pass
    
    def __getitem__(self, key):
        pass
    
    def __getattr__(self,*args, **kwargs):
        return args[0]
    
    def __dict__(self,*args, **kwargs):
        pass