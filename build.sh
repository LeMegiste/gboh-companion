#!/bin/bash
export JAVA_HOME=/Users/laurentkloetzer/code/jdk-11.0.12.jdk/Contents/Home/
echo $JAVA_HOME

export MVN_HOME=/Users/laurentkloetzer/code/apache-maven-3.8.2/bin


$MVN_HOME/mvn -Dhttps.protocols=TLSv1.2 install package;

export CMD="${JAVA_HOME}bin/java -jar target/gboh-companion.jar";

echo $CMD;

$CMD;




