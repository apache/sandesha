@echo off
set CLASSPATH=%CLASSPATH%;..\lib\axis-1.2-RC2.jar
set CLASSPATH=%CLASSPATH%;..\lib\addressing-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-ant-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-jaxrpc-1.2-RC2.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-saaj-1.2-RC2.jar
set CLASSPATH=%CLASSPATH%;..\lib\commons-discovery-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\commons-logging-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\junit-3.8.1.jar
set CLASSPATH=%CLASSPATH%;..\lib\wsdl4j-1.5-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\xerces.jar
 
java org.apache.axis.utils.Admin server ClientListenerDeploy.wsdd