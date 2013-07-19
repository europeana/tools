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


class ResetTranslationMiddleware(object):

    def process_request(self, request):
        reset_translation_cache()


def reset_translation_cache():
    from django.utils import translation
    from django.utils.translation import trans_real
    from threading import currentThread
    from django.conf import settings
    import gettext
    if settings.USE_I18N:
        try:
            # Reset gettext.GNUTranslation cache.
            gettext._translations = {}

            # Reset Django by-language translation cache.
            trans_real._translations = {}

            # Delete Django current language translation cache.
            trans_real._default = None

            # Delete translation cache for the current thread,
            # and re-activate the currently selected language (if any)
            prev = trans_real._active.pop(currentThread(), None)
            if prev:
                translation.activate(prev.language())
        except AttributeError:
            pass
