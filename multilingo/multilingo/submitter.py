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


from gen_utils.submit_base import DIR_STATIC_PAGES, DIR_MSG_KEYS, DIR_MSG_KEYS2, DIR_LOCALES, SYNC_INDICATOR, SubmitBase
from gen_utils.shell_cmd import cmd_execute


MAIL_FILE = '/tmp/multilingomail.txt'



class SubmitCommitter(SubmitBase):
    def run(self):
        if not os.path.exists(SYNC_INDICATOR):
            #self.log('No sync inddicator found, aborting')
            return

        # Since this might run longer than the crontab intervall, we store the recievers internally
        # and just leave a empty file as an indicator that we are in progress
        mail_recievers = open(SYNC_INDICATOR).readlines()
        if not mail_recievers:
            self.log('Svn syncing seems to be in progress, aborting')
            return
        os.remove(SYNC_INDICATOR)
        cmd_execute('touch %s' % SYNC_INDICATOR)
        
        self.log('Found sync indicator, commiting changes')
        subj = 'Multilingo submit'
        msg = 'The submit suceeded, portals will mail you as they pick up the changes'
        for path, label in ((DIR_MSG_KEYS, 'message properties portal1'),
                            (DIR_MSG_KEYS2, 'message properties portal'),
                            (DIR_STATIC_PAGES, 'static pages & support media'),
                            (DIR_LOCALES, 'locale source files'),
                            ):
            cmd = 'svn add --force *'
            self.log('%s %s' % (cmd, path))
            result = cmd_execute(cmd, path)
            if not result:
                cmd = "svn commit -m 'multilingo webcommit %s'" % label
                self.log('%s %s' % (cmd, path))
                result = cmd_execute(cmd, path)
            if result:
                self.log('Error from subshell: %s' % result)
                subj = '*** Error in multilingo submit'
                msg = 'Something went wrong when commiting %s, you could try again in a few minutes, if it still fails, report the bug\n\nDetails: %s' % (label, result)
                break
        for line in mail_recievers:
            self.log('will send mail to user:%s' % line)
            self.sendmail(subj, msg, line.strip())
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
