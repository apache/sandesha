@echo off
set CLASSPATH=%CLASSPATH%;..\lib\addressing-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-1.2-RC2.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-ant-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-jaxrpc-1.2-RC2.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-saaj-1.2-RC2.jar
set CLASSPATH=%CLASSPATH%;..\lib\commons-discovery-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\commons-logging-1.0.3.jar
set CLASSPATH=%CLASSPATH%;..\lib\junit-3.8.1.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-wsdl4j-1.2-RC1.jar
set CLASSPATH=%CLASSPATH%;..\lib\xerces.jar
set CLASSPATH=%CLASSPATH%;..\target\classes
 
java org.apache.axis.transport.http.SimpleAxisServer