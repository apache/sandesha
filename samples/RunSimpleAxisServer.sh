#!/bin/sh
CLASSPATH=.
CLASSPATH=$CLASSPATH:../lib/addressing-SNAPSHOT.jar
CLASSPATH=$CLASSPATH:../lib/axis-1.2.jar
CLASSPATH=$CLASSPATH:../lib/axis-ant-1.2.jar
CLASSPATH=$CLASSPATH:../lib/axis-jaxrpc-1.2.jar
CLASSPATH=$CLASSPATH:../lib/axis-saaj-1.2.jar
CLASSPATH=$CLASSPATH:../lib/commons-discovery-0.2.jar
CLASSPATH=$CLASSPATH:../lib/commons-logging-1.0.3.jar
CLASSPATH=$CLASSPATH:../lib/junit-3.8.1.jar
CLASSPATH=$CLASSPATH:../lib/axis-wsdl4j-1.2.jar
CLASSPATH=$CLASSPATH:../lib/log4j-1.2.8.jar
CLASSPATH=$CLASSPATH:../lib/xerces.jar

if [ -e Sandesha-samples.jar ]; then
    CLASSPATH=$CLASSPATH:Sandesha-samples.jar
if [ -e ../Sandesha-1.0-RC1.jar ]; then
     CLASSPATH=$CLASSPATH:../Sandesha-1.0-RC1.jar
     export CLASSPATH
     java -classpath $CLASSPATH org.apache.axis.transport.http.SimpleAxisServer
elif [ -e ../target/Sandesha-1.0-RC1.jar ]; then
     CLASSPATH=$CLASSPATH:../target/Sandesha-1.0-RC1.jar;
     export CLASSPATH
     java -classpath $CLASSPATH org.apache.axis.transport.http.SimpleAxisServer
else
echo "Cannot find the Sandesha-1.0-RC1.jar."
echo "If you are using the source distribution, please build the source using maven before running the samples"
fi
else
echo "Could not find Sandesha-samples.jar if you are using the source distribution, please"
echo "run the maven goal maven samples.jar."
fi
