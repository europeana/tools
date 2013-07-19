import urllib2
import sys

def check_url(url,s_expect):
    f = urllib2.urlopen(url, timeout=10)
    if f.code != 200:
        print 'ERROR: %s %s' % (f.code, f.msg)
        sys.exit(2)
    content = f.read()
    if content.find(s_expect) < 0:
        print 'ERROR %s not found' % s_expect
        sys.exit(2)
    return
        
        
        
progname="./check_http.py "
try:
    url=sys.argv[1]
    s_expect=sys.argv[2]
    
except:
    Help()
    sys.exit(2)

check_url(url,s_expect)
print 'OK - HTTP/1.1 200 OK'
