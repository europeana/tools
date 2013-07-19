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


# Copy to local_settings.py

DEBUG = False

import os.path
proj_root = os.path.normpath(os.path.dirname(__file__))


DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3', # Add 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': '%s/db.sqlite3' % proj_root,    # Or path to database file if using sqlite3.
        'USER': '',                      # Not used with sqlite3.
        'PASSWORD': '',                  # Not used with sqlite3.
        'HOST': '',                      # Set to empty string for localhost. Not used with sqlite3.
        'PORT': '',                      # Set to empty string for default. Not used with sqlite3.
    }
}


SECRET_KEY = '* long and secret string used as session key *'


# Should django handle static media?
# only activate this when debugging, never on production...
# where apache or similar should handle those requests
DELIVER_STATIC_MEDIA = False


# Where the europeana templates can be found
MEDIA_ROOT = '/Users/jaclu/proj/europeana/trunk/portal-full/src/main/webapp'


# The path to local media aditions, all content hear will be pushed to the
# production server and can be used for aditional css/js/imgs etc
#
MEDIA_FILE_PATH = 'sp'


#
# Where generated content for submission should be created
# this path will be cleared before each submit, so please dont point it to
# something like your home dir
#
# [[ If you havent read this and come to me with issues,  ]]
# [[ I will laught at you and point to this comment...    ]]
#
# As always keep your backups current!  ]]
#
SUBMIT_PATH = '/tmp/submit'


# If true SUBMIT_PATH is asumed to be set up for svn, and after each submit
# a svn commit -m 'multilingo submission - autocommited'
# will be done
#
SUBMIT_IS_SVN = True


#
# Normally progress is not displayed when translations are
# processed by the webserver, when fronted by apache
# apache can not tolerated anything to stdout
# enable this in test environs to get translation progess
#
TRANSLATIONS_ALLWAYS_SHOW_PROGRESS = False


# sorted in display order
# Syntax is: code to use, description
LANGUAGES = (
    ('ca', 'Catalan (ca)'),
    ('bg', 'Bulgarian (bul)'),
    ('cs', 'Czech (cze/cse)'),
    ('da', 'Dansk (dan)'),
    ('de', 'Deutsch (deu)'),
    ('el', 'Greek (ell/gre)'),
    ('en', 'English (eng)'),
    ('es', 'Espanol (esp)'),
    ('et', 'Eesti (est)'),
    ('fi', 'Suomi (fin)'),
    ('fr', 'Francais (fre)'),
    ('ga', 'Irish (gle)'),
    ('hu', 'Magyar (hun)'),
    ('is', 'Islenska (ice)'),
    ('it', 'Italiano (ita)'),
    ('lt', 'Lithuanian (lit)'),
    ('lv', 'Latvian (lav)'),
    ('mt', 'Malti (mlt)'),
    ('nl', 'Nederlands (dut)'),
    ('no', 'Norsk (nor)'),
    ('pl', 'Polski (pol)'),
    ('pt', 'Portuguese (por)'),
    ('ro', 'Romanian (rom'),
    ('sk', 'Slovkian (slo)'),
    ('sl', 'Slovenian (slv)'),
    ('sv', 'Svenska (sve/swe)'),
)

