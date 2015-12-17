













hexchars = '0123456789ABCDEF'

f_out = open('/tmp/triplet-retriever','w')
for a in hexchars:
    print 'A:', a
    for b in hexchars:
        f_out.write('echo "%s%s"\n' % (a,b))
        for c in hexchars:
            idx = a + b + c
            line = 'ssh euadmin@img2.europeana.sara.nl ls  /repository/%s | sort > triplets-%s\n' % (idx, idx)
            f_out.write(line)

f_out.close()
