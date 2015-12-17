import pstats
p = pstats.Stats('statfile-pstat')
#p.sort_stats('time').print_stats(10)
#p.sort_stats('time').print_stats(.5, 'init')

p.print_callers(.5, 'init')
