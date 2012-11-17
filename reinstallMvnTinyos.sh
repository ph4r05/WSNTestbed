#!/bin/bash

# get current directory 
DIR="$( cd -P "$( dirname "$0" )" && pwd )"; 
cd $DIR

if [ "$1x" == "x" ]; then
    echo "Please provide path to jar file to (tinyos.jar) as parameter"
    exit 1
fi

if [ ! -f $1 ]; then
    echo "Provided jar file does not exist"
    exit 1
fi

mvn install:install-file -Dfile=$1 -DgroupId=net -DartifactId=tinyos -Dversion=2.1ph4 -Dpackaging=jar
