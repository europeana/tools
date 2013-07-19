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

from django import forms
from django.contrib import admin
#from django.contrib import databrowse

import models


#databrowse.site.register(models.Aggregator)
#databrowse.site.register(models.Provider)
#databrowse.site.register(models.DataSet)
#databrowse.site.register(models.Request)



def make_active(modeladmin, request, queryset):
    queryset.update(processing=True)
make_active.short_description = "Process selected collections"

def make_inactive(modeladmin, request, queryset):
    queryset.update(processing=False)
make_inactive.short_description = "Stop processing of selected collections"


class PrioThumbsAdmin(admin.ModelAdmin):
    list_display = ('url',#'status',
                    )
    list_filter = ['status'] #,'concurrency']
    search_fields = ['url']

admin.site.register(models.PrioThumbs, PrioThumbsAdmin)


class ReqListAdmin(admin.ModelAdmin):
    list_display = ('name',#'concurrency',
                    'rec_count','file_date','last_processed','processing')
    actions = [make_active, make_inactive]
    list_filter = ['processing'] #,'concurrency']
    search_fields = ['name']


admin.site.register(models.ReqList, ReqListAdmin)
