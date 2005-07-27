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

if exist Sandesha-samples.jar goto samplesjarfound
echo Could not find Sandesha-samples.jar if you are using the source distribution, please
echo run the maven command "maven samples.jar".
pause
goto end

:samplesjarfound
set CLASSPATH=%CLASSPATH%;Sandesha-samples.jar;


if  exist  ..\Sandesha-1.0.jar goto  jarfoundinroot
if  exist  ..\target\Sandesha-1.0.jar goto  jarfoundintarget
echo Cannot find the Sandesha-1.0.jar.
echo If you are using the source distribution, please build the source using maven before running the samples
pause
goto end

:jarfoundinroot
set CLASSPATH=%CLASSPATH%;..\Sandesha-1.0.jar;

:jarfoundintarget
set CLASSPATH=%CLASSPATH%;..\target\Sandesha-1.0.jar;

java org.apache.axis.utils.Admin client ClientDeploy.wsdd
:end