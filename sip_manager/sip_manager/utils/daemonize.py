import grp
import os
import sys
from exceptions import OSError

def daemonize(stdin, stdout, stderr, pidfile):
    if os.path.isfile(pidfile):
        p = open(pidfile, "r")
        oldpid = p.read().strip()
        p.close()
        if os.path.isdir("/proc/%s"%oldpid):
            log.err("Server already running with pid %s"%oldpid)
            sys.exit(1)
    try:
        pid = os.fork()
        if pid > 0:
            sys.exit(0)
    except OSError, e:
        log.err("Fork #1 failed: (%d) %s"%(e.errno, e.strerror))
        sys.exit(1)
    os.chdir("/")
    os.umask(0)
    os.setsid()
    try:
        pid = os.fork()
        if pid > 0:
            if os.getuid() == 0:
                pidfile = open(pidfile, "w+")
                pidfile.write(str(pid))
                pidfile.close()
            sys.exit(0)
    except OSError, e:
        log.err("Fork #2 failed: (%d) %s"%(e.errno, e.strerror))
        sys.exit(1)
    """
    try:
        os.setgid(grp.getgrnam("nogroup").gr_gid)
    except KeyError, e:
        log.err("Failed to get GID: %s"%e)
        sys.exit(1)
    except OSError, e:
        log.err("Failed to set GID: (%s) %s"%(e.errno, e.strerror))
        sys.exit(1)
    try:
        os.setuid(pwd.getpwnam("oracle").pw_uid)
    except KeyError, e:
        log.err("Failed to get UID: %s"%e)
        sys.exit(1)
    except OSError, e:
        log.err("Failed to set UID: (%s) %s"%(e.errno, e.strerror))
        sys.exit(1)
    """
    for f in sys.stdout, sys.stderr:
        f.flush()
    if isinstance(stdin, basestring):
        si = open(stdin, "r")
    else:
        si = stdin
    if isinstance(stdout, basestring):
        so = open(stdout, "a+")
    else:
        so = stdout
    if isinstance(stderr, basestring):
        se = open(stderr, "a+", 0)
    else:
        se = stderr
    os.dup2(si.fileno(), sys.stdin.fileno())
    os.dup2(so.fileno(), sys.stdout.fileno())
    os.dup2(se.fileno(), sys.stderr.fileno())
