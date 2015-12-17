# -*- coding: utf-8 -*-
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



 Url structure for generated thumbnails, can be one of

 1 = - old style (pre 0.6)

        item with sha256 FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51

        would be saved as
        FE21/CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.FULL_DOC.jpg
        FE21/CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.BRIEF_DOC.jpg

 2 = from now on
        item with sha256 FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51

        would be saved as:
        original/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51
        FULL_DOC/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.jpg
        BRIEF_DOC/FE/21/FE21CB0D3B5C30C2AACD9026D5C445571FD0A932162872D7C45397DD65A51.jpg

        only FULL_DOC & BRIEF_DOC is sent to production

Old img generation
FULL_DOC
mogrify -path /dest_dir
    -format jpg
    -define jpeg:size=260x200
    -thumbnail 200x [one orginals sub dir]/*.original


BRIEF_DOC
mogrify -path BRIEF_DOC/subdir1/subdir2
    -format jpg
    -thumbnail x110 FULL_DOC/subdir1/subdir2/*.jpg
"""

import codecs
import copy
import datetime
import httplib
import os
import random
import sys
import time
import urllib2


# sudo ln -s /opt/local/lib/libMagickWand.dylib /opt/local/lib/libWand.dylib
#from pythonmagickwand.image import Image

from django.db import connection
from django.conf import settings
from django.utils.encoding import iri_to_uri

from utils.gen_utils import calculate_url_hash, calculate_hash
from utils.glob_consts import CONVERT_COMMAND, FULLDOC_SIZE, BRIEFDOC_SIZE
import utils.files_to_sync

from apps.log import models as log
from apps.sipmanager import sip_task



import models




HACK_QUCICHECK_OBJECTS = False


MIN_THUMB_AGE = time.mktime(time.localtime()) - settings.BACKLOG_AGE_MAX



URICREATE_LAST_RUN = 0



SIP_OBJ_FILES = settings.SIP_OBJ_FILES



URL_TIMEOUT = 10


HTTPH_CONT_LENGTH = 'content-length'
HTTPH_K_TRANS_ENC = 'transfer-encoding'
HTTPH_CHUNKED = 'chunked'

URI_DELIM = '://'


#ENC_STRAT_NONE = 'none'
#ENC_STRAT_FULL = 'full'


#URL_ENCODE_STRATEGIES = [
#   ENC_STRAT_NONE,
#    ENC_STRAT_FULL,
#    ]

"""
convert options that might be of interest to auto optimize images:
 -normalize
 -auto-level
 -contrast-stretch
 -linear-stretch
"""



# To avoid typos, we define the dirnames here and later use theese vars
REL_DIR_ORIGINAL = 'original'
REL_DIR_FULL = 'FULL_DOC'
REL_DIR_BRIEF = 'BRIEF_DOC'
#REL_DIR_TINY = 'TINY'






l = [
    "SELECT id FROM plug_uris_uri WHERE status=%i" % models.URIS_CREATED,
    "AND err_code=%i" % models.URIE_NO_ERROR,
    "AND uri_source=%i",
    "AND pid=0",
]
if HACK_QUCICHECK_OBJECTS:
    l.append("AND item_type=%i" % models.URIT_OBJECT)
l.append("ORDER by item_type")

SQL_VALIDATESAVE_FIND_ITEMS = " ".join(l)







class UriHandlerBase(sip_task.SipTask):
    urisource_cache = []
    def populate_urisource_cache(self,status=models.URIS_CREATED):
        cursor = connection.cursor()
        q = ["select distinct uri_source"]
        q.append("from plug_uris_uri")
        q.append("where err_code=0 and status=%i" % status)
        if HACK_QUCICHECK_OBJECTS:
            q.append("AND item_type=%i" % models.URIT_OBJECT)
        q.append("and uri_source in (select id from plug_uris_urisource where pid=0)")
        cursor.execute(" ".join(q))
        lst = []
        for i in cursor.fetchall():
            lst_id = i[0]
            lst.append(lst_id)

        random.shuffle(lst) # randomize to make sure we dont always pick them sequentally
        self.urisource_cache = lst
        return lst

    def get_first_available_urisource(self, status=models.URIS_CREATED):
        if not self.urisource_cache:
            self.populate_urisource_cache()
        if self.urisource_cache:
            r = self.urisource_cache.pop()
        else:
            r = 0
        return r

    def uri_state(self, state):
        self.uri.status = state
        self.uri.save()

    def file_name_from_hash(self, url_hash):
        fname = '%s/%s/%s' % (url_hash[:2], url_hash[2:4],url_hash)
        return fname




class UriPepareStorageDirs(sip_task.SipTask):
    SHORT_DESCRIPTION = 'Creates storage dirs'
    INIT_PLUGIN = True
    PLUGIN_TAXES_DISK_IO = True
    THREAD_MODE = sip_task.SIPT_NOT
    PRIORITY = sip_task.SIP_PRIO_HIGH

    def run_it(self):
        places = ((REL_DIR_ORIGINAL, False),
                  (REL_DIR_FULL, False), (REL_DIR_BRIEF, False))

        for s, old_style in places:
            if old_style:
                tst_dir = '6EA' # just test something random for existance
            else:
                tst_dir = '12/32'  # just test something random for existance

            test_dir = os.path.join(SIP_OBJ_FILES, s, tst_dir)
            if not os.path.exists(test_dir):
                self.task_starting('Creating dirs for %s' % s, 256)
                if old_style:
                    self.pre_generate_uri_trees_old_style(s)
                else:
                    self.pre_generate_uri_trees(s)
        return True

    def pre_generate_uri_trees(self, prefix):
        hex_str = '0123456789ABCDEF'
        base_dir = os.path.join(SIP_OBJ_FILES, prefix)
        if not os.path.exists(base_dir):
            os.makedirs(base_dir)
        for f1 in hex_str:
            for f2 in hex_str:
                first_dir = os.path.join(base_dir, '%s%s' % (f1, f2))
                self.safe_mkdir(first_dir)
                for s1 in hex_str:
                    for s2 in hex_str:
                        second_dir = os.path.join(first_dir, '%s%s' % (s1, s2))
                        self.safe_mkdir(second_dir)


    def pre_generate_uri_trees_old_style(self, prefix):
        hex_str = '0123456789ABCDEF'
        base_dir = os.path.join(SIP_OBJ_FILES, prefix)
        if not os.path.exists(base_dir):
            self.safe_mkdir(base_dir)
        for f1 in hex_str:
            for f2 in hex_str:
                for f3 in hex_str:
                    new_dir = os.path.join(base_dir, '%s%s%s' % (f1, f2, f3))
                    self.safe_mkdir(new_dir)


    def safe_mkdir(self, ddir):
        try:
            os.makedirs(ddir)
            return True
        except OSError,e:
            if e.errno == 17:
                pass # dir existed
            else:
                raise OSError,e
        return False



class UriValidateSave(UriHandlerBase):
    SHORT_DESCRIPTION = 'process new uri records'
    PLUGIN_TAXES_NET_IO = True
    THREAD_MODE = sip_task.SIPT_THREADABLE
    PRIORITY = sip_task.SIP_PRIO_LOW

    def prepare(self):
        uri_source = self.get_first_available_urisource(models.URIS_CREATED)
        if not uri_source:
            return False
        self.urisource = models.UriSource.objects.filter(pk=uri_source)[0]
        self.initial_message = self.urisource.name_or_ip
        return True


    def run_it(self):
        # We must mark this item early, before possible threading kicks in
        # otherwise we might find it again before the thread actually starts
        # to work on the current set
        self.urisource = self.grab_item(models.UriSource, self.urisource.pk,'processing imgs for source')
        if not self.urisource:
            return False

        self.modified_files = utils.files_to_sync.AddingFilesToSync(self.pid, self.log)
        self.log('logging added thumbnails to: %s' % self.modified_files.abs_filename, 7)
        cursor = connection.cursor()
        cursor.execute(SQL_VALIDATESAVE_FIND_ITEMS % self.urisource.pk + " LIMIT 3000")
        uri_count = cursor.rowcount
        self.task_starting(self.urisource.name_or_ip, uri_count, display=False)

        idx = 0
        t_timeout = time.time() + 3600 * 1 # hours max processing time
        self.task_set_label(self.urisource.name_or_ip)
        for row in cursor.fetchall():
            t0 = time.time() + 1
            uri_id = row[0]
            idx += 1
            self.cached_state = 0
            try:
                s2 = 'http://oai-pmh.nid.pl:5070/nid-ws/rest/oaipmh/getImgNormal?url=carare/Pomniki_Historii/Katowice_-_Gmach_Wojew%C3%B3dztwa_i_Sejmu_%C5%9Al%C4%85skiego/NID-pomhis-Katowice_Gmach-003.jpg'
                u2 = u'http://oai-pmh.nid.pl:5070/nid-ws/rest/oaipmh/getImgNormal?url=carare/Pomniki_Historii/Katowice_-_Gmach_Wojew%C3%B3dztwa_i_Sejmu_%C5%9Al%C4%85skiego/NID-pomhis-Katowice_Gmach-003.jpg'
                s_no_utf81stline = u'http://oai-pmh.nid.pl:5070/nid-ws/rest/oaipmh/getImgMin?url=carare/Pomniki_Historii/Katowice_-_Gmach_Wojew\xf3dztwa_i_Sejmu_\u015al\u0105skiego/NID-pomhis-Katowice_Gmach-003.jpg'
                self.uri = models.Uri.objects.filter(pk=uri_id)[0]
            except:
                msg = 'ERROR: uri_id [%s] not found' % uri_id
                self.log(msg)
                print msg
                raise
            self.handle_uri()  # normal operation
            if self.cached_state:
                self.uri_state(self.cached_state)

            try:
                self.task_time_to_show(idx, terminate_on_high_load=True)
            except sip_task.SipSystemOverLoaded:
                 # Terminate in a controled fashion so we can do cleanup
                break


            if idx > uri_count:
                # More items has turned up since we started this loop,
                # not a problem as such but in order to show a correct eta and stuff
                # we should terminate now, and things will continue on next call
                # to this plugin
                break

            if time.time() > t_timeout:
                self.log('*** Uri checking for %s terminated due to max processing time' % self.urisource.name_or_ip, 1)
                break
            # max one / sec per webserver thread if we download the img, for just checking links do it without delay
            if self.uri.item_type == models.URIT_OBJECT:
                while time.time() < t0:
                    time.sleep(0.02)

        self.log('FilesToSync, moving %s to sync-wait' % self.modified_files.abs_filename, 7)
        self.modified_files.close_adding()
        self.release_item(models.UriSource, self.urisource.pk)
        return True

    def handle_uri(self):
        url_itm = self.verify()
        if not url_itm:
            return False

        if self.uri.item_type == models.URIT_OBJECT:
            # we only download objects
            if not self.save_object(url_itm):
                return False

        self.cached_state = models.URIS_COMPLETED
        return True


    def verify(self):
        #global URL_ENCODE_STRATEGIES
        "Check if url is responding and giving a 200 result."
        self.uri.time_lastcheck = datetime.datetime.now()

        itm = None

        try:
            #for u_mthd in URL_ENCODE_STRATEGIES:
            url = iri_to_uri(self.uri.url)
            #if url.find('Gmach-003.jpg') > -1:
            #    pass
            itm = urllib2.urlopen(urllib2.Request(url, headers = {'User-Agent':'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36'}),
                                  timeout=URL_TIMEOUT)

            if not itm:
                raise
        except urllib2.HTTPError, e:
            try:
                err_msg = httplib.responses[e.code]
            except:
                err_msg = 'Unable to lookup error code'
            return self.set_urierr(models.URIE_HTTP_ERROR, '[%i] - %s' % (e.code, err_msg))

        except urllib2.URLError, e:
            if str(e.reason) == 'timed out':
                code = models.URIE_TIMEOUT
                msg = ''
            else:
                code = models.URIE_URL_ERROR
                msg =  str(e.reason)
            return self.set_urierr(code, msg)
        except:
            return self.set_urierr(models.URIE_OTHER_ERROR, 'Unhandled error when checking url')

        if itm.code != 200:
            try:
                err_msg = httplib.responses[itm.code]
            except:
                err_msg = 'Unable to lookup error code'
            return self.set_urierr(models.URIE_HTML_ERROR, '[%s] - %s' % (itm.code, err_msg) )
        try:
            content_t = itm.headers['content-type']
        except:
            return self.set_urierr(models.URIE_MIMETYPE_ERROR, 'Failed to parse mime-type')

        self.uri.mime_type = content_t[:models.MIME_TYPE_FIELD_LENGTH-1]
        self.cached_state = models.URIS_VERIFIED
        return itm


    def save_object(self, itm):
        if self.uri.mime_type.find('text/') > -1:
            return self.set_urierr(models.URIE_WAS_HTML_PAGE_ERROR)

        for bad_groups in ('audio','video'):
            if self.uri.mime_type.find(bad_groups) > -1:
                return self.set_urierr(models.URIE_UNSUPORTED_MIMETYPE_ERROR)
        headers = itm.headers
        if headers.has_key(HTTPH_K_TRANS_ENC) and (headers[HTTPH_K_TRANS_ENC] == HTTPH_CHUNKED):
            content_length = 0
        else:
            try:
                content_length = int(itm.headers[HTTPH_CONT_LENGTH])
            except:
                # previously we aborted, if content lenght couldnt be read
                # this proved to be to restrictive, now we just log a warning
                # and accept the item
                el = log.ErrLog(err_code=log.LOGE_WEB_SERV_RESP,
                                msg = 'Failed to read %s' % HTTPH_CONT_LENGTH,
                                item_id = '%s %i' % (self.uri._meta.db_table, self.uri.pk),
                                plugin_module = self.__class__.__module__,
                                plugin_name = self.__class__.__name__)
                el.save()
                content_length = 0
        try:
            data = itm.read()
        except:
            return self.set_urierr(models.URIE_DOWNLOAD_FAILED,
                                   'Failed to read object content')

        if content_length and (len(data) != content_length):
            return self.set_urierr(models.URIE_WRONG_FILESIZE,
                                   'Wrong filesize, expected: %i recieved: %i' % (
                                       content_length, len(data)))

        self.uri.content_hash = calculate_hash(data)

        #
        #  Store original
        #
        org_rel = self.file_name_from_hash(self.uri.content_hash)
        org_fname = os.path.join(SIP_OBJ_FILES, REL_DIR_ORIGINAL, org_rel)
        try:
            fp = open(org_fname, 'w')
            fp.write(data)
            fp.close()
        except:
            return self.set_urierr(models.URIE_FILE_STORAGE_FAILED,
                                   'Failed to save original')
        # self.modified_files.add(org_fname)
        self.cached_state = models.URIS_ORG_SAVED

        # Identify & store actual filetyp
        retcode, stdout, stderr = self.cmd_execute_output('identify %s[0]' % org_fname)
        if retcode:
            msg = u'retcode: %s\nstdout: %s\nstderr: %s' % (retcode, stdout, stderr)
            return self.set_urierr(models.URIE_OTHER_ERROR,
                                   'Failed to identify file type\n%s' % msg)
        try:
            f_type = stdout.split(org_fname)[-1].strip()
            if f_type[0] == ':':
                f_type = f_type[1:].strip()
            self.uri.file_type = f_type[:149]
        except:
            return self.set_urierr(models.URIE_UNRECOGNIZED_FORMAT,
                                   'Failed to parse filetype')
        if f_type.lower().find('html') > -1:
            return self.set_urierr(models.URIE_WAS_HTML_PAGE_ERROR,
                                   'mime_type image, content html')

        try:
            self.uri.url_hash = calculate_url_hash(self.uri.url)
        except:
            return self.set_urierr(models.URIE_OBJ_CONVERT_ERROR, 'Failed to caclculate urlhash')
        try:
            s_org_w, s_org_h = f_type.split()[1].split('x')
            self.uri.org_w = int(s_org_w)
            self.uri.org_h = int(s_org_h)
        except:
            return self.set_urierr(models.URIE_OTHER_ERROR, 'Failed to find img size')

        thumb_fname = self.file_name_from_hash(self.uri.url_hash)
        return self.generate_images(thumb_fname, org_fname, self.uri.org_w, self.uri.org_h)




    def url_parse(self, url, method):

        try:
            # 1 check if url is proper notation
            result = str(url)
        except:
            try:
                # 2 handle generic utf-u8 case
                es = codecs.encode(url,'utf-8')
                s_quoted = urllib2.quote(es)
                result = str(s_quoted)
            except:
                self.log('ERROR: failed to parse url %s' % url, 0)
                result = ''
        return result
    """                try:
                    sr = urlparse.urlsplit(url.encode('utf-8').replace('%20', ' '))
                    try:
                        result = sr.scheme + '://' + urllib.quote(sr.netloc, safe=':') + urllib.quote(sr.path,safe='/')
                        if sr.query:
                            result += '?' + sr.query
                    except:
                        result = None
                        try:
                            self.log('ERROR: failed to urlquote %s' % self.uri.url, 0)
                        except:
                            self.log('ERROR: failed to urlquote for uri.id %s' % self.uri.id, 0)
                        result = self.uri.url

    def update_url_encode_strategies(self, u_mthd):
        if not self._url_methds_remaining:
            self._url_methds_remaining = copy.deepcopy(URL_ENCODE_STRATEGIES)
        self._url_methds_remaining.remove(u_mthd)
        self._url_methods_failed.append(u_mthd)
    """


    def set_urierr(self, code, msg=''):
        if code not in models.URI_ERR_CODES:
            raise SipTaskException('set_urierr called with invalid errcode')
        #if self.cached_state:
        #    self.uri.status = self.cached_state
        self.cached_state = 0 # make sure no state is saved
        self.uri.err_code = code
        if msg:
            self.uri.err_msg = msg
        else:
            # give name of error as default message
            self.uri.err_msg = models.URI_ERR_CODES[code]

        """        # Only mark as failed if we didnt make any progress at all
        if (self.uri.status == models.URIS_CREATED) and code in (
            models.URIE_TIMEOUT, models.URIE_HTTP_ERROR,
            models.URIE_HTML_ERROR, models.URIE_URL_ERROR):
            self.uri.status = models.URIS_FAILED
        """
        self.uri.status = models.URIS_FAILED
        self.uri.save()
        return False # propagate error



    def generate_images(self, base_fname, org_fname, org_w, org_h):
        if not self.generate_fulldoc(base_fname, org_fname, org_w, org_h):
            return False
        self.cached_state = models.URIS_FULL_GENERATED

        if not self.generate_briefdoc(base_fname, org_fname, org_w, org_h):
            return False
        self.cached_state = models.URIS_BRIEF_GENERATED
        return True


    def generate_fulldoc(self, base_fname, org_fname, org_w, org_h):
        fname_full = os.path.join(SIP_OBJ_FILES, REL_DIR_FULL,
                                  '%s.jpg' % base_fname)
        cmd = [CONVERT_COMMAND]
        if (org_w > FULLDOC_SIZE[0]) or (org_h > FULLDOC_SIZE[1]):
            #cmd.append('-resize 200x380')
            cmd.append('-resize %ix%i' % FULLDOC_SIZE)
        else:
            pass
        cmd.append('%s[0]' % org_fname)
        cmd.append(fname_full)
        retcode, stdout, stderr = self.cmd_execute_output(cmd)
        if retcode:
            self.remove_file(fname_full)
            return self.set_urierr(models.URIE_OBJ_CONVERT_ERROR,
                                   'Failed to generate FULL_DOC\ncmd output: %s%s' % (stdout,stderr))
        if stdout or stderr:
            el = log.ErrLog(err_code=log.LOGE_IMG_CONV_WARN,
                            msg = u'FULL_DOC %s %s' % (stdout, stderr),
                            item_id = '%s %i' % (self.uri._meta.db_table, self.uri.pk),
                            plugin_module = self.__class__.__module__,
                            plugin_name = self.__class__.__name__)
            el.save()
            return self.set_urierr(models.URIE_OBJ_CONVERT_ERROR,
                                   'Failed to generate FULL_DOC\ncmd output: %s%s' % (stdout,stderr))
        else:
            self.log('Created FULL image %s' % fname_full, 9)
        self.modified_files.add(fname_full)
        return True


    def generate_briefdoc(self, base_fname, org_fname, org_w, org_h):
        fname_brief = os.path.join(SIP_OBJ_FILES, REL_DIR_BRIEF,
                                   '%s.jpg' % base_fname)
        cmd = [CONVERT_COMMAND]
        if org_w > BRIEFDOC_SIZE[0] or org_h > BRIEFDOC_SIZE[1]:
            #cmd.append('-resize 160x110')
            cmd.append('-resize %ix%i' % BRIEFDOC_SIZE)
        else:
            pass
        cmd.append('%s[0]' % org_fname)
        cmd.append(fname_brief)
        retcode, stdout, stderr = self.cmd_execute_output(cmd)
        if retcode:
            self.remove_file(fname_brief)
            return self.set_urierr(models.URIE_OBJ_CONVERT_ERROR,
                                   'Failed to generate BRIEF_DOC\ncmd output: %s%s' % (stdout,stderr))
        if stdout or stderr:
            el = log.ErrLog(err_code=log.LOGE_IMG_CONV_WARN,
                            msg = u'BRIEF_DOC %s %s' % (stdout, stderr),
                            item_id = '%s %i' % (self.uri._meta.db_table, self.uri.pk),
                            plugin_module = self.__class__.__module__,
                            plugin_name = self.__class__.__name__)
            el.save()
            return self.set_urierr(models.URIE_OBJ_CONVERT_ERROR,
                                   'Failed to generate BRIEF_DOC\ncmd output: %s%s' % (stdout,stderr))
        else:
            self.log('Created BRIEF image %s' % fname_brief, 9)
        self.modified_files.add(fname_brief)
        return True




    def remove_file(self, fname):
        if not SIP_OBJ_FILES in fname:
            raise SipTaskException('Attempt to remove illegal filename: %s' % fname)
        try:
            os.remove(fname)
        except OSError:
            # maybe it wasnt created, who cares, at least it's gone now
            pass
        return






class UriHandleBacklog(UriHandlerBase):
    SHORT_DESCRIPTION = 'Handling the backlog after lost DB'
    PLUGIN_TAXES_CPU = True
    PLUGIN_TAXES_DISK_IO = True
    THREAD_MODE = sip_task.SIPT_THREADABLE
    PRIORITY = sip_task.SIP_PRIO_NORMAL


    def prepare(self):
        uri_source = self.get_first_available_urisource(models.URIS_CHECK_BACKLOG)
        if not uri_source:
            return False
        self.urisource = models.UriSource.objects.filter(pk=uri_source)[0]
        self.initial_message = self.urisource.name_or_ip
        return True

    def run_it(self):
        self.urisource = self.grab_item(models.UriSource, self.urisource.pk,'processing imgs for source')
        if not self.urisource:
            return False

        cursor = connection.cursor()
        sql = ["SELECT id FROM plug_uris_uri"]
        sql.append("WHERE status=%i" % models.URIS_CHECK_BACKLOG)
        sql.append("AND pid=0 AND uri_source=%i " % self.urisource.pk)
        sql.append("LIMIT 7500")
        cursor.execute(" ".join(sql))
        uri_count = cursor.rowcount
        self.task_starting(self.urisource.name_or_ip, uri_count, display=False)
        idx = 0
        for row in cursor.fetchall():
            uri_id = row[0]
            idx += 1
            self.uri = models.Uri.objects.filter(pk=uri_id)[0]
            if (self.uri.item_type == models.URIT_OBJECT) and self.thumb_fresh_n_done():
                uri_state = models.URIS_COMPLETED
            else:
                # never fail here, this test has no info regarding to bad links
                # only on pre existing files, if no file tag it for later
                # processing by UriValidateSave()
                uri_state = models.URIS_CREATED

            self.uri_state(uri_state)

            try:
                self.task_time_to_show(idx, terminate_on_high_load=True)
            except sip_task.SipSystemOverLoaded:
                 # Terminate in a controled fashion so we can do cleanup
                break

            if idx > uri_count:
                # More items has turned up since we started this loop,
                # not a problem as such but in order to show a correct eta and stuff
                # we should terminate now, and things will continue on next call
                # to this plugin
                break

        self.release_item(models.UriSource, self.urisource.pk)
        return True


    def thumb_fresh_n_done(self):
        """
        Only return true if a thumbnail can be found based on this uri, and that it is fresh "enough"
        """
        try:
            thumb_hash = calculate_url_hash(self.uri.url)
        except:
            return False # failed to generate hash, leave it to normal code to show a good error
        base_fname = self.file_name_from_hash(thumb_hash)
        fname_full = os.path.join(SIP_OBJ_FILES, REL_DIR_FULL, '%s.jpg' % base_fname)
        try:
            if os.path.getmtime(fname_full) >= MIN_THUMB_AGE:
                self.uri.mime_type ='cached'
                self.uri.file_type = 'thumbnail'
                self.uri.url_hash = thumb_hash
                return True
        except:
            return False # file not found
        return False






# List of active plugins from this file
task_list = [
    UriPepareStorageDirs,
    UriValidateSave,
    UriHandleBacklog,
    # obsolete UriCreate,
]



class FixUriDb(object):

    def run(self):
        uri_new = models.Uri()
        for ouri in models.OldUri.objects.all():
            a = uri_new.add(ouri)