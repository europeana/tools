set ANNOCULTOR_HOME=%CD%
set ANNOCULTOR_COLLECTION=%CD%
set MAVEN_OPTS=-Xmx1024m
set ANNOCULTOR_TOOLS_JAR=tools/target/tools-2.3.1.jar
echo Warning! Using tools-2.3.1.jar tja would only work with annocultor-2.3.1
call "3rdparty/apache-maven-2.2.1/bin/mvn.bat" %1 %2 %3 %4 %5 %6 %7