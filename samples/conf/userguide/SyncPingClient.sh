#!/bin/sh

export SANDESHA2_CLASS_PATH=./UserguideSampleClients.jar
AXIS2_CLASSPATH=$AXIS2_CLASSPATH:SGCCalculator.jar
for f in ./lib/*.jar
do
  SANDESHA2_CLASS_PATH=$SANDESHA2_CLASS_PATH:$f
done

java -cp %SANDESHA2_CLASS_PATH% sandesha2.samples.userguide.SyncPingClient ./