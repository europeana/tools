import os
import psycopg2

conn = psycopg2.connect("host=carola dbname=thumblr2 user=sipmanager password=Op6oODq8mTDO" )
cur = conn.cursor()

cur.execute("SELECT id, ffile FROM dataset_dataset ORDER BY ffile;")
for ds_id, ffile in cur.fetchall():
  label = ffile.split('datasets/')[1].split('.tgz')[0]

  print label

  sql = ["SELECT u.url"]
  sql.append('FROM dataset_dataseturls du, plug_uris_uri u')
  sql.append('WHERE du.ds_id = %i and u.id = du.uri_id' % ds_id)
  sql.append('order by RANDOM() LIMIT 2000')

  cur.execute(' '.join(sql))

  fname = os.path.join('/tmp/randomlinks', label)
  f = file(fname,'w')
  for line in cur.fetchall():
    url = line[0]
    #print url
    f.write('%s\n' % url)
  f.close()
