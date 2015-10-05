"""Replay requests from a log file, subclass with handlers for the specific format.
- Takes time between requests into account, with option to speed up the replay.
  Inspired by https://github.com/chromano/apache-log-replay
  Initially Written 2015-09-30 by jacob.lundqvist@europeana.eu
  License: EUPL

  Tested with Python 2.7.6, 3.4.3

  To use on your own logfiles, implement your own parse_logline() and you should be ready to go
  I have suplied a few reference implemenations you can use as template.

  Typical hooks for subclasses:

    custom_options(self, parser)
        Add your extra options here

    verify_options(self, parser)
        Check that your extra param(-s) are valid

    parse_logline(self, line):
        Override to handle your specific log format, should return a timestamp, and the finalised url
            ts, url
            if the line couldnt be parsed, return 0, None
"""


import tempfile
import time
import requests
from optparse import OptionParser
import multiprocessing


class LogReplayer(object):
    def __init__(self):
        """Parse command line options."""
        usage = "usage: %prog [options] logfile\n\n" \
                "  Typical usage cases:\n\n" \
                "    regular replay of logs\n" \
                "       potentially use -s to speed it up\n\n" \
                "    replay logfile at full speed sequentially\n" \
                "       -1\n\n" \
                "    stress test, ignoring timestamps (where X is number of worker threads)\n" \
                "       -i -m X\n" \

        parser = OptionParser(usage)
        parser.add_option('-s', '--speedup',
            help='make time run faster by factor SPEEDUP',
            dest='speedup',
            type='int',
            default=1)
        parser.add_option('-1', '--singlethread',
                          help='run new request as soon as one is completed, ignoring timestamps',
                          dest='sinlge', action="store_true", default=False)
        parser.add_option('-m', '--maxworkers',
                          help='max pending requests (defaults to 15)',
                          dest='max_workers', type='int', default=15)
        parser.add_option('-i', '--ignorets',
                          help='ignore timestamps (-m controlls number of concurrent threads)',
                          dest='use_timestamps', action="store_false", default=True)
        parser.add_option('-p', '--progress',
                          help='progress intervall (defaults to 3)',
                          dest='progress', type='int', default=3)
        self.custom_options(parser)
        (self.options, args) = parser.parse_args()
        if len(args) == 1:
            self.fname = args[0]
        else:
            parser.error("incorrect number of arguments (try -h)")
        if self.options.sinlge and (self.options.speedup > 1):
            parser.error('You can not specify both -s and -1 (try -h)')
        if self.options.max_workers < 1:
            parser.error('max_workers must be > 0')
        self.verify_options(parser)
        self.num_lines = 0 # file length logfile
        self.urls_processed = 0 # urls sent
        self.urls_processed_last = 0 # used to calculate delta between updates
        self.workers = {} # active workers
        self.failed_requests = {} # index of all failed requests, defined as non 200 results
        self.timings = []
        self.queue_results = multiprocessing.Queue()

    def custom_options(self, parser):
        "Use this as hook for defining custom options"
        return

    def verify_options(self, parser):
        "Use this hook to verify that self.options (typically custom_options) are valid"
        return


    def logfile_read(self):
        """returns timestamp + query param, one line at a time"""
        first_line = True
        ts_offset = 0
        with open(self.fname, 'r') as fh:
            for line in fh:
                #print ('logfile_read() processing line')
                ts, url = self.parse_logline(line[:-1])
                if first_line and ts:
                    ts_offset = ts
                    first_line = False
                yield ts - ts_offset, url

    def run(self):
        print('')
        self.num_lines = sum(1 for line in open(self.fname))
        print('Processing requests from: %s - contains %i lines (some might not be useable)' % (self.fname, self.num_lines))
        if self.options.sinlge:
            self.options.use_timestamps = False
            self.options.max_workers = 1
            print('Will run in sequential mode and send the next request as soon as the previous returns')
        if self.options.use_timestamps:
            print('Will replay the logfile at %i times the original speed based on timestamps (adjust with -s)' % self.options.speedup)
        else:
            print('Will try to keep all assigned workers occupied, ignoring timestamps')
        print('Will use a maximum of %i workers (adjust with -m)' % self.options.max_workers)
        print('Status will be updated every %i seconds (adjust with -p)' % self.options.progress)
        print('')
        self.lines_processed = 0
        t_offset = self.t_progress = time.time()
        for ts, url in self.logfile_read():
            self.lines_processed += 1
            if not url:
                # not usable line in the logfile
                continue
            if self.options.use_timestamps:
                 # wait until we are ready for next line accd to logfile timing
                while (ts/self.options.speedup) > (time.time() - t_offset):
                     time.sleep(0.001)
            #
            # Make sure we stay within limits and dont do an unintentional denial of service...
            #
            while len(self.workers) >= self.options.max_workers:
                self.maybe_show_progres()
                time.sleep(0.01)
            else:
                self.maybe_show_progres()

            self.handle_request(url)

        print('Waiting for all requests to complete...')
        while len(self.workers):
            self.maybe_show_progres()
        print('>>>>> logfile completed, re-played %i urls <<<<<' % self.urls_processed)
        if self.failed_requests:
            f = tempfile.NamedTemporaryFile('w', prefix='logreplay-', delete=False)
            print('Failed requests, by kind and count (saved to %s)' % f.name)
            f.write('logfile being re-played: %s\n' % self.fname)
            for key in self.failed_requests:
                msg = '%s %i' %(key, len(self.failed_requests[key]))
                print(msg)
                f.write(msg + '\n')
                for url in self.failed_requests[key]:
                    f.write('\t%s\n' % url.encode('utf-8'))
            f.close()
        else:
            print('\tAll requests succeeded!')

    def handle_request(self, url):
        p = multiprocessing.Process(target=self.request_worker, args=(self.queue_results, url))
        p.start()
        while not p.pid:
            # wait for it to start
            print ('_')
            time.time(0.001)
        self.workers[p.pid] = p
        self.urls_processed += 1

    def request_worker(self, queue, url):
        try:
            #print(url) # only use for debug...
            headers = {'User-Agent': 'logreplaylib.py 1.0'}
            r = requests.get(url, headers=headers, timeout=30)
            status_code = r.status_code
            response_time = r.elapsed.microseconds / 1000000.0
        except:
            # probably no such host or similar
            status_code = 'other'
            response_time = 0.001
        p = multiprocessing.current_process()
        queue.put({'url' :url,
                   'status' :status_code,
                   'pid' :p.pid,
                   'response_time' :response_time})

    def maybe_show_progres(self, process_queue = True):
        if process_queue:
            self.process_completed_tasks()
        if self.t_progress + self.options.progress < time.time():
            self.t_progress += self.options.progress
            self.show_progress()
        return

    def process_completed_tasks(self):
        while not self.queue_results.empty():
            result = self.queue_results.get()
            p = self.workers[result['pid']]
            p.join() # let it finish so we dont end up with zombies...
            del self.workers[result['pid']]
            self.timings.append(result['response_time'])

            status = result['status']
            if not status == 200:
                if status not in self.failed_requests.keys():
                    self.failed_requests[status] = []
                self.failed_requests[status].append(result['url'])
        return

    def show_progress(self):
        completed = len(self.timings)

        parts = ['Sent: %i (%.1f%%)' % (self.urls_processed, self.lines_processed/float(self.num_lines or 1.0)*100)]
        parts.append('pending: %i' % len(self.workers))

        delta = (completed - self.urls_processed_last) / float(self.options.progress or 1)
        self.urls_processed_last = completed
        parts.append('completed/s: %.1f' % delta)

        average = sum(self.timings) / float((completed or 1))
        parts.append('avg.resp time: %.1f' % average)

        failed = 0

        for k in self.failed_requests.keys():
            failed += len(self.failed_requests[k])
        failed_ratio = failed/((self.urls_processed - len(self.workers)) or 1) * 100
        if failed_ratio:
            parts.append('fail%% : %.1f' % failed_ratio)

        if self.options.use_timestamps and (len(self.workers) >= self.options.max_workers):
            parts.append('>> All workers waiting for server! <<')
        msg = ' \t'.join(parts)
        print(msg)



    # =============================================================================
    #
    # Override parse_logline()
    #
    # =============================================================================

    def parse_logline(self, line):
        """Override to handle your specific log format, should return a timestamp, and the finalised url
            ts, url
            if the line couldnt be parsed, return 0, None
        """
        ts = time.time()
        url = 'http://europeana.eu'
        return ts, url







