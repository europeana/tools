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

 Generic utility functions
"""

import datetime
import os

from django.conf import settings


try:
    os.makedirs(settings.TIMESTAMP_FILES)
except:
    pass


class TimeStampFile(object):
    def __init__(self):
        self.fname = os.path.join(settings.TIMESTAMP_FILES, self.__class__.__name__)
        
    def set(self):
        open(self.fname,'w').write('')
        
    def clear(self):
        try:
            os.remove(self.fname)
        except OSError:
            pass
        
    def get(self):
        try:
            st = os.stat(self.fname)
        except:
            return 'Timestamp not available'
        r = datetime.datetime.fromtimestamp(st.st_mtime).isoformat()
        return r

class TimeStampSyncImgs(TimeStampFile):
    pass

class TimeStampLinkStats(TimeStampFile):
    pass
