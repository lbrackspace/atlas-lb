#!/bin/sh
 
for jar in lib/*.jar
do
  CLASSPATH="$CLASSPATH:$jar"
done
export CLASSPATH="$CLASSPATH:dist/java-cloudfiles.jar:."

java com.rackspacecloud.client.cloudfiles.sample.FilesCli $@
