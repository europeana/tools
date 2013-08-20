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

import os
import time

import settings





def ensure_dir_exists(ddir):
    if os.path.exists(ddir):
        return
    os.makedirs(ddir)



DIR_STATIC_PAGES = os.path.join(settings.SUBMIT_PATH, 'static_pages') # portal2
ensure_dir_exists(DIR_STATIC_PAGES)

DIR_MESSAGE_KEYS = os.path.join(settings.SUBMIT_PATH, 'message_keys')
ensure_dir_exists(DIR_MESSAGE_KEYS)

DIR_GETTEXT_SRC = os.path.join(settings.SUBMIT_PATH, 'gettext-src')
ensure_dir_exists(DIR_GETTEXT_SRC)


SYNC_INDICATOR = '/tmp/multilingo-sync'




LOG_FILE = '/tmp/multilingo.log'



class SubmitBase(object):
    def log(self, msg):
        s = '[%s] (pid %i) %s\n' % (time.asctime(), os.getpid(), msg)
        open(LOG_FILE, 'a').write(s)
        
