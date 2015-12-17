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


from django.contrib import admin
from django.contrib import messages

from models import TranslatePage, RightsPage, Template, MediaIMG, MediaJS, MediaCSS





def make_active(modeladmin, request, queryset):
    queryset.update(active=True)
make_active.short_description = "Production activate selected pages"

def make_inactive(modeladmin, request, queryset):
    queryset.update(active=False)
make_inactive.short_description = "Production inactivate selected pages"





class TranslatePageAdmin(admin.ModelAdmin):
    list_display = ('file_name', 'active')
    actions = [make_active, make_inactive]




admin.site.register(TranslatePage, TranslatePageAdmin)
admin.site.register(RightsPage, TranslatePageAdmin)

admin.site.register(Template)

admin.site.register(MediaIMG)
admin.site.register(MediaJS)
admin.site.register(MediaCSS)

