@echo off
set CLASSPATH=%CLASSPATH%;..\lib\axis-1.2-RC2.jar
set CLASSPATH=%CLASSPATH%;..\lib\commons-logging-1.0.3.jar
set CLASSPATH=%CLASSPATH%;..\lib\commons-discovery-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\xerces.jar
set CLASSPATH=%CLASSPATH%;..\target\classes
java org.apache.axis.utils.Admin client ClientDeploy.wsdd
 
 