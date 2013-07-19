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

 server runner
"""

from optparse import make_option

from django.core.management.base import BaseCommand

from apps.sipmanager.processor import MainProcessor


class Command(BaseCommand):
    option_list = BaseCommand.option_list + (
        #make_option('--single-run', action='store_true', dest='single-run', default=False,
        #    help='Process queue once then terminate.'),
        #make_option('--find-dupes', action='store_true', dest='find-dupes', default=False,
        #    help='Find records with duplicate fields.'),
        #make_option('--clear-bad-links', action='store_true', dest='clear-bad-links', default=False,
        #    help='Clear all bad links, they will be processed again'),
        #make_option('--clear-aborted-links', action='store_true', dest='clear-aborted-links', default=False,
        #    help='Reset all halfprocessed links, they will be processed again.'),
        make_option('--flush-all', action='store_true', dest='flush-all', default=False,
            help='Completely erase all data.'),
        make_option('--drop-all', action='store_true', dest='drop-all', default=False,
            help='Remove all tables from db.'),
        make_option('--clear-pids', action='store_true', dest='clear-pids', default=False,
            help='Clear all process monitoring data.'),
        #make_option('--no-upsizing', action='store_true', dest='no-upsizing', default=False,
        #    help='Find all small imgs and regenerate without upsizing.'),
        #make_option('--without-size', action='store_true', dest='without-size', default=False,
        #    help='Find all unsized imgs and make sure no upscaling is done.'),
        #make_option('--mongify', action='store_true', dest='mongify', default=False,
        #    help='Move content to mongo db.'),
        #make_option('--newrequest-model', action='store_true', dest='newrequest-model', default=False,
        #    help='New request model - rune once to sync db.'),
        #make_option('--sync-imgs', action='store_true', dest='sync-imgs', default=False,
        #    help='Sync images.'),
        #make_option('--update-reqlist', action='store_true', dest='update-reqlist', default=False,
        #    help='Update list of available requests.'),
        make_option('--update-reqstats', action='store_true', dest='update-reqstats', default=False,
                    help='Update request statistics.'),
    )
    help = """Runs all checks and keeps track of running processes.
    Also responsible for filesystem cleanup actions.

    Some extra modes, they will terminate when they are done.

  ??  --single-run
        Perform all pendning tasks then exit

    Not normaly used:    
       ?? --find-dupes /path/to/file.xml
            Find records with duplicate key items in suplied file
    
       ?? --no-upsizing
            Find all small imgs and regenerate without upsizing.
           
       ?? --without-size
            Clear all bad links, they will be processed again
           
       ?? --mongify
            Move content to mongo db.
       
      ??  --newrequest-model
           New request model - rune once to sync db.
       
        
    --flush-all
        Completeley erase everything - Dangerous!

    --drop-all
        Removes all sip_manager tables - Dangerous!
        extra param force removes also expensive tables

    ?? --clear-bad-links
        Clear all bad links, they will be processed again
       
    ?? --clear-aborted-links
        Reset all halfprocessed links, they will be processed again

    ?? --clear-pids
        Clear all process-monitoring data
       
    ?? --sync-imgs
       Sync images.
    
    ?? --update-reqlist
        Update list of available requests.
       
    ?? --update-reqstats
       Update request statistics.
       
    """
    args = ''#[--daemon]'

    def handle(self, *args, **options):
        mp = MainProcessor(options, *args)
        mp.run()

