"""Replay requests from a log file, subclass with handlers for the specific format.
- Takes time between requests into account, with option to speed up the replay.
  Inspired by https://github.com/chromano/apache-log-replay
  Written 2015-09-30 by jacob.lundqvist@europeana.eu
  License: EUPL

  The single threaded mode is actually still using subprocesses, but waits for them to complete before running the next
"""

import subprocess
import sys
import time
import requests
from optparse import OptionParser
import multiprocessing


class LogReplayer(object):
    TIME_PROGRESS  = 2 # intervall for showing progress

    def __init__(self):
        """Parse command line options."""
        usage = "usage: %prog [options] logfile"
        parser = OptionParser(usage)
        parser.add_option('-s', '--speedup',
            help='make time run faster by factor SPEEDUP',
            dest='speedup',
            type='int',
            default=1)
        parser.add_option('-1', '--singlethread',
                          help='run new request as soon as one is completed',
                          dest='sinlge', action="store_true", default=False)
        parser.add_option('-m', '--maxworkers',
                          help='max pending requests',
                          dest='max_workers', type='int', default=15)
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
        self.workers_running = 0
        self.urls_processed = 0
        self.queue_results = multiprocessing.Queue()
        self.workers = {}
        self.failed_requests = {}
        self.timings = []

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
        if self.options.sinlge:
            print('Will run in sequential mode and send the next request as soon as the previous returns')
        else:
            print('Will replay the logfile at %i times the original speed based on timestamps (adjust with -s)' % self.options.speedup)
        print('Status will be updated every %i seconds' % self.TIME_PROGRESS)
        print('Processing timestamps and requests from: %s' % self.fname)
        print('Will use a maximum of %i workers (adjust with -m)' % self.options.max_workers)
        t_offset = self.t_progress = time.time()
        idx = 0
        for ts, url in self.logfile_read():
            if not url:
                continue # not usable line in the logfile
            idx += 1
            has_waited = False
            if not self.options.sinlge:
                 # wait until we are ready for next line accd to logfile timing
                while (ts/self.options.speedup) > (time.time() - t_offset):
                     if not has_waited:
                         has_waited = True
                     time.sleep(0.001)
            self.handle_request(url)
            if self.options.sinlge:
                # in single threaded mode, wait for task to complete
                while self.workers_running:
                    time.sleep(0.01)
                    self.process_completed_tasks()
                    self.maybe_show_progres()
            else:
                self.maybe_show_progres()
            while self.workers_running >= self.options.max_workers:
                self.process_completed_tasks()
                time.sleep(0.01)

        print('Waiting for all requests to complete...')
        while self.workers_running:
            self.maybe_show_progres()
        print('>>>>> logfile completed <<<<<')


    def handle_request(self, url):
        p = multiprocessing.Process(target=self.request_worker, args=(self.queue_results, url))
        p.start()
        while not p.pid:
            # wait for it to start
            print ('_')
            time.time(0.001)
        self.workers[p.pid] = p
        self.workers_running += 1
        self.urls_processed += 1

    def request_worker(self, queue, url):
        """
        This basic method only curls the url not caring about success/fails if you want to process the output
        override this
        """
        if url.find('"') > -1:
            print('found double quote in url [%s], that is not tested...' % url)

        #print('requesting: %s' % url)
        try:
            r = requests.get(url)
            status_code = r.status_code
            response_time = r.elapsed.microseconds / 1000000
        except:
            # probably no such host or similar
            status_code = 999
            response_time = 0.01
        p = multiprocessing.current_process()
        queue.put({'url' :url,
                   'status' :status_code,
                   'pid' :p.pid,
                   'response_time' :response_time})

    def maybe_show_progres(self):
        if self.t_progress + self.TIME_PROGRESS < time.time():
            self.t_progress += self.TIME_PROGRESS
            self.handle_progress()
        return

    def handle_progress(self):
        self.process_completed_tasks()
        completed = len(self.timings)
        failed = 0
        for k in self.failed_requests.keys():
            failed += len(self.failed_requests[k])
        try:
            average = sum(self.timings) / float(completed)
        except:
            average = 0
        count = self.urls_processed
        succeeded = completed - failed
        try:
            failed_ratio = failed/(count - self.workers_running) * 100
        except:
            failed_ratio = 0
        msg = 'Sent:%i\t pending:%i\t fail ratio:%.1f' % (self.urls_processed, self.workers_running, failed_ratio)
        if self.options.sinlge:
            msg += '\t sequential mode'
        else:
            msg += '\t speed:%i' % self.options.speedup
        print(msg)



    def process_completed_tasks(self):
        while not self.queue_results.empty():
            result = self.queue_results.get()
            p = self.workers[result['pid']]
            p.join() # let it finish so we dont end up with zombies...
            del self.workers[result['pid']]
            self.workers_running -= 1
            self.timings.append(result['response_time'])

            status = result['status']
            if not status == 200:
                if status not in self.failed_requests.keys():
                    self.failed_requests[status] = []
                self.failed_requests[status].append(result['url'])
        return



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







