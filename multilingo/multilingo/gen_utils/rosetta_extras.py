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


from django.conf import settings

from shell_cmd import cmd_execute



def translator_allowed(user, lang):
    """Europeana specific modification
    Only allow translators to modify the languages they are responsible for

    modify rosetta/views.py (aprox #239)
    for language in settings.LANGUAGES:
        # Patch to only allow translator to handle assigned language
        if not gen_utils.translator_allowed(request.user, language[0]):
            continue

    """
    a = user.get_all_permissions()
    b = user.has_perm('rosetta.%s' % lang) or user.has_perm('rosetta.all_langs')
    return b




def trigger_webserver_reload():
    cmd_execute('%s' % settings.proj_root)
