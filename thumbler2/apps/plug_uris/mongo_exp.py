from django.db import connection

from utils.mongo import exporter

import models


def uri(sip_task, collection):
    def item_mapper(old_uri):
        cursor = connection.cursor()
        cursor.execute('SELECT r.file_name FROM plug_uris_requri ru, dummy_ingester_request r WHERE ru.uri_id=%i AND r.id=ru.req' % old_uri.id)
        req_d = {} 
        for row in cursor.fetchall():
            req_d[row[0]]=1
        reqs = list(req_d) # using a dict to remove dupes..

        return {                
            '_id': old_uri.url_hash,
            'item_type': models.URI_TYPES[old_uri.item_type],
            'url': old_uri.url,
            'uri_source': old_uri.uri_source.name_or_ip,
            # 'url_hash': old_uri.url_hash, is in _id

            'content_hash': old_uri.content_hash,
            'mime_type': old_uri.mime_type,
            'file_type': old_uri.file_type,
            'org_w': old_uri.org_w,
            'org_h': old_uri.org_h,

            'status': models.URI_STATES[old_uri.status],
            'err_code': models.URI_ERR_CODES[old_uri.err_code],
            'err_msg': old_uri.err_msg,

            'time_created': old_uri.time_created,
            'time_lastcheck': old_uri.time_lastcheck,
            
            'requests': reqs,
            'mdrs':[old_uri.mdr.content_hash],
            }
    exporter(sip_task, collection, models.Uri, item_mapper)
