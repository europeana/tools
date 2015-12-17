Debug paramas for manage.py

runserver --noreload --nothreading 10.101.28.57:8586
sipmanager --clear-pids
sipmanager --update-reqstats
---

3007852





---------------------


Finding a record asociated with a thumbnail
Portal search:
provider_aggregation_edm_object:"http://partage.aveiro.pt/images/AN-2192_d.jpg”

use name of dataset as portal search
europeana_collectionName:2026120*

on a given record:  &format=labels





---------- Listing uris by collection name


select  u.url
from plug_uris_urisource us
     ,plug_uris_uri u
     ,dataset_dataseturls du
     ,dataset_dataset ds
where ds.ffile like '%15404%'
  and du.ds_id = ds.id
  and u.id = du.uri_id

limit 10


----   all collections with zero uris
select ds_id, set_name, record_count
from statistics_statistics
where
-- set_name like '20226%'
  record_count = 0
order by set_name


----- {not done} remove all uris for a given dataset
select ds_id
   -- , set_name
  from statistics_statistics
  where
  -- set_name like '2022%'
  record_count = 0
  and count_1 = 0
  order by set_name








-- finding all urisource with unprocessed items
select distinct uri_source
from plug_uris_uri
where err_code=0 and status=1
and uri_source in (select id from plug_uris_urisource where pid=0)

--------------------


------- clearing dupes from dataset_dataseturls
DELETE FROM dataset_dataseturls USING dataset_dataseturls du2
 WHERE dataset_dataseturls.ds_id = du2.ds_id
   AND dataset_dataseturls.uri_id = du2.uri_id
   AND dataset_dataseturls.id < du2.id;
-------


----------- clear all errors
update plug_uris_uri

set status=1,
 mime_type='',
 file_type='',
 url_hash='',content_hash='',
 err_code=0,err_msg=''

where err_code > 0

-----------


---------
select distinct d.ffile, count(d.ffile)
from plug_uris_uri u, dataset_dataseturls du, dataset_dataset d
where
  -- u.uri_source in (select id from plug_uris_urisource where pid=0) and
  -- u.err_code=0 and
  u.status=1
  and du.uri_id=u.id
  and d.id = du.ds_id
group by d.ffile
----------




--- undo all urls arround a power outage or other crash
update plug_uris_uri

set status=1,
 mime_type='',
 file_type='',
 url_hash='',content_hash='',
 err_code=0,err_msg=''



where time_lastcheck > '2014-02-06 08:58:30'
  and time_lastcheck < '2014-02-06 10:00:00'

---



---- url status and count by collection name ---
select u.status, count(u.status)
from plug_uris_uri u, dataset_dataseturls du, dataset_dataset ds
where ds.ffile like '%9200237%'
  and du.ds_id = ds.id
  and u.id = du.uri_id
group by u.status


---- detailed err count by collection name ----
select u.err_code, count (u.err_code)
from plug_uris_uri u, dataset_dataseturls du, dataset_dataset ds
where ds.ffile like '%11604%'
  and du.ds_id = ds.id
  and u.id = du.uri_id
group by u.err_code
order by u.err_code


--- clear all errors for a named collection ----
UPDATE plug_uris_uri
SET status=1 -- URIS_CREATED
  ,mime_type=''
  ,file_type=''
  , org_w=0, org_h=0
  , pid=0
  , url_hash=''
  , content_hash=''
  , err_code=0 -- URIE_NO_ERROR
  , err_msg=''

WHERE id in (
  SELECT du.uri_id
  FROM dataset_dataseturls du, dataset_dataset ds
  WHERE ds.ffile LIKE '%11604%'
    AND du.ds_id = ds.id
  )
  -- AND item_type= 1 -- URIT_OBJECT
  AND err_code != 0




--- count occupied plug_uris_uri per collection ID ---
select count(u.pid)
from plug_uris_uri u, dataset_dataseturls du, dataset_dataset ds
where u.pid > 0
  and u.id = du.uri_id
  and du.ds_id = ds.id
  and ds.ffile like '%11604%'


---- url uri source and count by collection ID ---
select us.id, us.name_or_ip, count(us.name_or_ip)
from plug_uris_urisource us
     ,plug_uris_uri u
     ,dataset_dataseturls du
     ,dataset_dataset ds
where ds.ffile like '%11604%'
  and du.ds_id = ds.id
  and u.id = du.uri_id
  and us.id = u.uri_source
group by us.id, name_or_ip

---- busy uri sources ---
select count(*)
from plug_uris_urisource
where
pid > 0


-------- mark all urisources occupied except for specific dataset
UPDATE plug_uris_urisource
SET pid=-99
WHERE id not in (
select us.id
from plug_uris_urisource us
     ,plug_uris_uri u
     ,dataset_dataseturls du
     ,dataset_dataset ds
where ds.ffile like '%11604%'
  and du.ds_id = ds.id
  and u.id = du.uri_id
  and us.id = u.uri_source
group by us.id, name_or_ip
)


------------

statistics uris not generated
