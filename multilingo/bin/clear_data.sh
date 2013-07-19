#!/bin/sh

#
# When syncing between diff
#
#

cd /Users/jaclu/proj/europeana/multilingo/multilingo

echo
echo "This tool is used to clear all userdata from multilingo, then run import_multilingo_data.sh"
echo "to repopulate with new content"
echo
echo "Make sure this is the expected place, should be <projroot>/multilingo"
echo "  (normaly ending with multilingo/multilingo)"
echo
pwd

echo
echo "If you dont hit Ctrl-C all translation content WILL be removed in 5 seconds!"
sleep 5

rm apps/multi_lingo/locale/??/LC_MESSAGES/*.mo
rm apps/multi_lingo/locale/??/LC_MESSAGES/*.po

rm apps/multi_lingo/templates/static_pages/*

rm support_media/css/*
rm support_media/img/*
rm support_media/js/*

rm apps/multi_lingo/templates/*.html



