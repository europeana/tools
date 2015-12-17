from django.contrib import admin


from models import DataSet, DataSetUrls, DS_CREATED





class DataSetAdmin(admin.ModelAdmin):
    fields = ['ffile','status']
    
    
    list_display = ('id','ffile', 'status','err_code','err_msg','time_created')
    actions = ['reprocess_Dataset', 'delete_DataSet']
    #list_filter = ['processing'] #,'concurrency']
    search_fields = ['ffile']    
    #radio_fields = {"status": admin.HORIZONTAL}
    
    
    #
    #  Filter out default delete_selected
    #
    def get_actions(self, request):
        actions = super(DataSetAdmin, self).get_actions(request)
        if 'delete_selected' in actions:
            del actions['delete_selected']
        return actions    
    
    def reprocess_Dataset(self, request, queryset):
        ds_count = len(queryset)
        for ds in queryset:
            ds.status = DS_CREATED
            ds.save()
        self.message_user(request, "datasets resceduled for processing.")        
    reprocess_Dataset.short_description = "Reprocess selected data sets"
        
    def delete_DataSet(self, request, queryset):
        ds_count = len(queryset)
        for ds in queryset:
            DataSetUrls.objects.filter(ds=ds).delete()
            ds.delete()
        if ds_count == 1:
            message_bit = "1 data set was"
        else:
            message_bit = "%s data sets were" % ds_count        
        self.message_user(request, "%s successfully deleted." % message_bit)        
    delete_DataSet.short_description = "Delete selected data sets"


    
    
admin.site.register(DataSet, DataSetAdmin)

