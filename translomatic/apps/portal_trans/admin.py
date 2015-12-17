from django.contrib import admin

# Register your models here.

from .models import TranslatePage,RightsPage

class TranslatePageAdmin(admin.ModelAdmin):
    list_display = ('pk', 'file_name', 'last_changed')




admin.site.register(TranslatePage, TranslatePageAdmin)

admin.site.register(RightsPage)
