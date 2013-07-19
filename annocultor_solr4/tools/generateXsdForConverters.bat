set ANNOCULTOR_TOOLS_JAR=%JAVA_HOME%/jre/lib/tools.jar
mvn exec:exec -Dexec.args="-cp %%classpath %MAVEN_OPTS% eu.annocultor.xconverter.impl.Converter profile\\profile-%1.xml tmp/work"
