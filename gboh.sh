#!/bin/bash
export JAVA_HOME=/Users/laurentkloetzer/code/jdk-11.0.12.jdk/Contents/Home/
echo $JAVA_HOME


export CMD="${JAVA_HOME}bin/java -jar target/gboh-companion.jar";

echo $CMD;

$CMD;




