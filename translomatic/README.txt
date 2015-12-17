Tool for creating translations for portal 1 & 2

Obsoleted as of 2015-12-17





Dependency:

    Rosetta https://github.com/mbi/django-rosetta




python manage.py makemigrations portal_trans
python manage.py migrate



old settings:


#
#======================   Rosetta specifics   =================================
#

# Number of messages to display per page.
ROSETTA_MESSAGES_PER_PAGE = 10

# Enable Google translation suggestions
ROSETTA_ENABLE_TRANSLATION_SUGGESTIONS = False

# Displays this language beside the original MSGID in the admin
#ROSETTA_MAIN_LANGUAGE = 'en'

# Change these if the source language in your PO files isn't English
#ROSETTA_MESSAGES_SOURCE_LANGUAGE_CODE = 'en'
#ROSETTA_MESSAGES_SOURCE_LANGUAGE_NAME = 'English'

"""
When running WSGI daemon mode, using mod_wsgi 2.0c5 or later, this setting
controls whether the contents of the gettext catalog files should be
automatically reloaded by the WSGI processes each time they are modified.

Notes:

 * The WSGI daemon process must have write permissions on the WSGI script file
   (as defined by the WSGIScriptAlias directive.)
 * WSGIScriptReloading must be set to On (it is by default)
 * For performance reasons, this setting should be disabled in production environments
 * When a common rosetta installation is shared among different Django projects,
   each one running in its own distinct WSGI virtual host, you can activate
   auto-reloading in individual projects by enabling this setting in the project's
   own configuration file, i.e. in the project's settings.py

Refs:

 * http://code.google.com/p/modwsgi/wiki/ReloadingSourceCode
 * http://code.google.com/p/modwsgi/wiki/ConfigurationDirectives#WSGIReloadMechanism

"""
ROSETTA_WSGI_AUTO_RELOAD = False
ROSETTA_UWSGI_AUTO_RELOAD = False

# Exclude applications defined in this list from being translated
ROSETTA_EXCLUDED_APPLICATIONS = (
    'django.contrib.admindocs',
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.sites',
    'django.contrib.messages',
    'django.contrib.sessions',
    'django.contrib.contenttypes',
    'multilingo.rosetta',
)

ROSETTA_USE_FUZZY = False
