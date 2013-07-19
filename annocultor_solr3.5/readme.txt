To build these projects from Eclipse you need:

start Eclipse and choose
- File/import/existing project to workspace starting from this directory

set up variable ANNOCULTOR_HOME pointing to here twice:
- as Windows environment variable (so that temp location may be determined at run time)
   My Computer/Properties/Advanced/Environment variables/restart Eclipse
- as Eclipse path variable (so that build path can be resolved)
   Eclipse AnnoCore project properties/Java build path/libraries/Add variable/Configure variables/New/multiple 'cancel'

allocate enough memory by setting option -Xmx1024m or more if you have more
(with the limit of 60% of available physical memory)
  Window/preferences/Java/installed JREs/edit/default JM arguments, add -Xmx1024m

Launch configurations are saved in the project directories, we recommend to
follow this practice.

==========================================================================

- Separate XML files can be merged and processed with a single converter run
- Local.build.properties file allows setting build properties for ant build (e.g. -Xmx) 
- Build numbers in both build system and source
- Local file annocultor.properties may override environment properties and ANNOCULTOR_HOME
- New conversion report generated into the /doc directory 

==========================================================================
28-07-2008
Version 1.3, build 1

- streaming conversion, flat memory profile, but vocabularies are stored in memory
- all tests are automaticallt extracted
- Path allows expressions like element[attr='value' and attr='value'] in queries
- Path allows expressions like element@attr in queries returning attribute value
- Environment stores a number of directories
- Conversion stores the RDF to be overwritten into /prev, after conversion it does a diff stored in /diff


