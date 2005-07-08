#!/bin/sh
CLASSPATH='.'
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
export CLASSPATH
java org.apache.axis.utils.tcpmon
