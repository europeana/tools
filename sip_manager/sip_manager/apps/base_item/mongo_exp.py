from django.db import connection

from utils.mongo import exporter

import models


def mdr(sip_task, collection):
    def item_mapper(old_mdr):
        cursor = connection.cursor()
        cursor.execute('SELECT r.file_name FROM base_item_requestmdrecord m, dummy_ingester_request r WHERE m.md_record_id=%i AND r.id=m.request_id' % old_mdr.id)
        req_d = {} 
        for row in cursor.fetchall():
            req_d[row[0]]=1
        reqs = list(req_d) # using a dict to remove dupes..

        return {                
            '_id': old_mdr.content_hash,
            'source_data': old_mdr.source_data,
            'status': models.MDRS_STATES[old_mdr.status],
            
            'uniqueness_hash': old_mdr.uniqueness_hash,
            'enrichment_done': old_mdr.Enrichment_done,
            
            'time_created': old_mdr.time_created,
            'time_last_change': old_mdr.time_last_change,

            'requests': reqs,
        }
    exporter(sip_task, collection, models.MdRecord, item_mapper)

def dataset(sip_task, collection):
    def item_mapper(old_mdr):
        return {                
            '_id': old_mdr.content_hash,
            'source_data': old_mdr.source_data,
            'status': models.MDRS_STATES[old_mdr.status],
            
            'uniqueness_hash': old_mdr.uniqueness_hash,
            'enrichment_done': old_mdr.Enrichment_done,
            
            'time_created': old_mdr.time_created,
            'time_last_change': old_mdr.time_last_change,

            'requests': reqs,
        }
    exporter(sip_task, collection, models.MdRecord, item_mapper)
