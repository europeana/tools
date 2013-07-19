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

from django.conf.urls.defaults import url, patterns

#from views import find_templates, show_page, prop_page, PROP_URL_NAME
#import views


class SubmitError(Exception):
     def __init__(self, value):
          self.value = value
     def __str__(self):
          return repr(self.value)


def global_environ(request):
    """Insert some additional information into the template context
    from the settings.
    Specifically, the LOGOUT_URL, MEDIA_URL and BADGES settings.
    """
    additions = {
        #'DJANGO_ROOT': request.META['SCRIPT_NAME'],
        'europeana_item_count_mill': '23.4', # in milions how large the dataset is
    }
    return additions



