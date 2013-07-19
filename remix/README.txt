=== Installing on a new system

1 Create database from template in db/remix.sql
2 Copy content of etc/templates/ to etc
3 Change the files to match your environment
4 change etc/settings.php to match your environment
5 point apache to use your config file and restart apache

Done!
  

=== Apache owned parts...

chown www-data: -R remix/website/client/data




=== various debian dependencies 

debs: libapache2-mod-php5 php5-cli php5-common php5-curl php5-mysql

apache/modes-enabled

Dont know if it matters but somebody bothered to rename the
rewrite.load soft link to start withh 000 - to ensure its parsed first??

00rewrite.load  ../mods-available/rewrite.load
proxy_balancer.load
proxy.conf
proxy_http.load
proxy.load
headers.load


=== js and css minification

there's a Makefile in the /tools directory that will use the closure compiler
and yuicompressor to compress and minify the js and css assets

1. cd tools
2. make
