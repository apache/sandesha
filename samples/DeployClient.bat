@echo off
set CLASSPATH=%CLASSPATH%;..\lib\addressing-SNAPSHOT.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-1.2.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-ant-1.2.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-jaxrpc-1.2.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-saaj-1.2.jar
set CLASSPATH=%CLASSPATH%;..\lib\commons-discovery-0.2.jar
set CLASSPATH=%CLASSPATH%;..\lib\commons-logging-1.0.3.jar
set CLASSPATH=%CLASSPATH%;..\lib\junit-3.8.1.jar
set CLASSPATH=%CLASSPATH%;..\lib\axis-wsdl4j-1.2.jar
set CLASSPATH=%CLASSPATH%;..\lib\log4j-1.2.8.jar
set CLASSPATH=%CLASSPATH%;..\lib\xerces.jar
if  exist  ..\Sandesha-beta.jar goto  jarfoundinroot
if  exist  ..\target\Sandesha-beta.jar goto  jarfoundintarget
echo Cannot find the Sandesha-beta.jar.
echo If you are using the source distribution, please build the source using maven before running the samples
pause
goto end

:jarfoundinroot
set CLASSPATH=%CLASSPATH%;..\Sandesha-beta.jar;

:jarfoundintarget
set CLASSPATH=%CLASSPATH%;..\target\Sandesha-beta.jar;

java org.apache.axis.utils.Admin client ClientDeploy.wsdd
:end