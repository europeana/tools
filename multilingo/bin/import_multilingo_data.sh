#!/bin/sh

cd /Users/jaclu/proj/europeana/multilingo/multilingo


echo
echo "This tool is used to fill a (hopefully empty) multilingo with"
echo " userdata exported from another system"
echo
echo "Make sure this is the expected place, should be <projroot>/multilingo"
echo "  (normaly ending with multilingo/multilingo)"
echo
pwd

echo
echo "If you dont hit Ctrl-C all translation content WILL be overwritten in 5 seconds!"
sleep 5

cat ~/tmp/multilingo/dump.sql | mysql multilingo

tar xvfz ~/tmp/multilingo/locale.tgz

tar xvfz ~/tmp/multilingo/static_pages.tgz

tar xvfz ~/tmp/multilingo/support_media.tgz

tar xvfz ~/tmp/multilingo/templates.tgz
