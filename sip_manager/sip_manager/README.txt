

Requirements

1. Django
2. python-psycopg2 (for postgresql)
2. django-dajax
    http://wiki.github.com/jorgebastida/django-dajax/


change:
http://www.dlib.si/StreamDB.aspx?URN=URN:NBN:SI:doc-1XKQP735
into:
http://www.dlib.si/v2/StreamDb.aspx?URN=URN:NBN:SI:doc-1XKQP735



python-django
    djblets
    dmigrations  south
    lint



portal search for a named dataset:
europeana_collectionName:01406*




mysql:
ALTER TABLE `plug_uris_uri` ADD `org_w` INT NOT NULL AFTER `file_type` , ADD `org_h` INT NOT NULL AFTER `org_w`


postgres:
ALTER TABLE plug_uris_uri ADD org_w INT NOT NULL default 0; ALTER TABLE plug_uris_uri ADD org_h INT NOT NULL default 0




create index base_item_mdrecord_unprocessed ON base_item_mdrecord (urls_extracted,pid)
create index plug_uris_uri_unprocessed ON plug_uris_uri (uri_source,status,err_code,pid,mdr_pk)




Should be run from cron:
--update-reqstats

sipmanager --update-reqstats
runserver --noreload


"DROP TABLE IF EXISTS dup_list_new; CREATE TABLE dup_list_new AS select r.req, count(u.id), u.url from plug_uris_requri r, plug_uris_uri u where u.id = r.uri group by r.req, to_char(u.mdr_pk, '999999') || u.url, u.url having count(u.id) > 1 order by r.req; DROP TABLE IF EXISTS dup_list; ALTER TABLE dup_list_new RENAME TO dup_list;


explain UPDATE plug_uris_uri
SET status=1 ,mime_type='', file_type='', pid=0, url_hash=''
    ,content_hash='', err_code=0, err_msg=''
WHERE status != 100
AND status != 999








/// new commit syntax
Story 232.2, thumblr, ensuring a syncfile isnt aborted by the normal plugin one hour max runtime


// sync issue?

jacwork-124.35979 546/356037 44 (0.15%) 	12-02 08:20:54 	11-29 14:00:0


apps/sipmanager/sync_imgs.py line 25 start sync loop early
temp mark non TINY groups as inactive in local_settings



runserver 127.0.0.1:9790 --noreload --nothreading