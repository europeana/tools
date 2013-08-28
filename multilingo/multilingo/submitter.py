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

from email.mime.text import MIMEText
import os
import smtplib

import git

import settings

from gen_utils.submit_base import SYNC_INDICATOR, SubmitBase
from gen_utils.shell_cmd import cmd_execute


MAIL_FILE = '/tmp/multilingomail.txt'

try:
    FORCE_RUN = settings.FORCE_SUBMIT
except:
    FORCE_RUN = False # only enable when debugging...


class SubmitCommitter(SubmitBase):
    def run(self):
        if (not os.path.exists(SYNC_INDICATOR)) and (not FORCE_RUN):
            #self.log('No sync inddicator found, aborting')
            return

        # Since this might run longer than the crontab intervall, we store the recievers internally
        # and just leave a empty file as an indicator that we are in progress
        if not FORCE_RUN:
            mail_recievers = open(SYNC_INDICATOR).readlines()
            if not mail_recievers:
                self.log('Svn syncing seems to be in progress, aborting')
                return
        else:
            mail_recievers = ()
        try:
            os.remove(SYNC_INDICATOR)
        except:
            # we only removed a file, if it was already gone, not a biggie...
            pass
        cmd_execute('touch %s' % SYNC_INDICATOR)
        
        self.log('Found sync indicator, commiting changes')
        subj = 'Multilingo submit'
        msg = 'The submit suceeded, portals will mail you as they pick up the changes'
        repo = git.Repo(settings.SUBMIT_PATH)
        if repo.is_dirty():
            try:
                a = b = c = ''
                print '*** will log'
                self.log('git add -A: %s' % settings.SUBMIT_PATH)
                print '*** git add'
                a = repo.git.add('-A')
                self.log('git commit')
                print '*** git commit'
                b = repo.git.commit('-m "multilingo machine commit"')
                self.log('git push')
                print '*** git push'
                c = repo.git.push()
            except:
                msg = ''
                for s in a,b,c:
                    if s:
                        msg += '[%s] ' % s
                self.log('Error git push failed: %s' % msg )
                subj = '*** Error in multilingo push'
                msg = 'Something went wrong when commiting portal_translations, you could try again in a few minutes, if it still fails, report the bug\n\nDetails: %s' % msg
        else:
            msg = 'No changes needed to be commited'
                
        for aadr in mail_recievers:
            self.log('will send mail to user:%s' % aadr)
            self.sendmail(subj, msg, aadr.strip())
        self.log('commit done')
        os.remove(SYNC_INDICATOR)

           
    def sendmail(self, subj, body, recv):
        "Simple mailsend using sendmail cmd"
        self.log('sendmail(%s) %s' % (recv, subj))
        fromaddr = 'multilingo-submitter'
        msg = MIMEText(body)        
        msg['From'] = fromaddr
        msg['To'] = recv
        msg['Subject'] = subj
        b_result=False
        try:
            smtpObj = smtplib.SMTP('localhost')
            smtpObj.sendmail(fromaddr, recv, msg.as_string())        
            self.log('Successfully sent email')
            b_result=True
        except smtplib.SMTPException:
            self.log("Error: unable to send email")
        return b_result

    
if __name__ == "__main__":
    sc = SubmitCommitter()
    sc.run()
