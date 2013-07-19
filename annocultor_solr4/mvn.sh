#! /bin/sh
echo "mvn"
# export M2_HOME=3rdparty/apache-maven-2.2.1/
export ANNOCULTOR_HOME=`pwd`
export MAVEN_OPTS=-Xmx1024m
. 3rdparty/apache-maven-2.2.1/bin/mvn -P development "$1" "$2" "$3" "$4" "$5" "$6" "$7"
