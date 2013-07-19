import codecs
import sys

rec_identifier = 'europeana:uri'

def display_recid(record):
    foo,s = record.split('<%s>' % rec_identifier)
    sid,foo = s.split('</%s' % rec_identifier)
    return sid

def main(field_name, file_name):
    print 'Displaying %s for each record with duplicate %s' % (rec_identifier, field_name)
    print 'reading file %s ...' % file_name
    data = codecs.open(file_name, 'r', 'utf-8').read()
    records = data.split('</record>')
    print 'found %i records' % len(records)
    idx = 0
    count = 0
    s_param = '<%s>' % field_name
    for record in records:
        idx += 1
        if record[record.find(s_param)+1:].find(s_param) > -1:
            count += 1
            #print 'item no', idx
            print display_recid(record)

    print 'Found %i duplicates' % count

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print 'duplicate fields fieldname file'

    main(sys.argv[1], sys.argv[2])
