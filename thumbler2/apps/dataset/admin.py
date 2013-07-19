from django.contrib import admin

from models import DataSet


class DataSetAdmin(admin.ModelAdmin):
    fields = ['ffile','pid']
    
admin.site.register(DataSet, DataSetAdmin)

