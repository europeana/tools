Debug paramas for manage.py

runserver --noreload --nothreading
sipmanager --clear-pids


select u.status, count(u.status) 
from plug_uris_uri u, dataset_dataseturls du
where u.id = du.uri_id
  and du.ds_id = 3
group by status
