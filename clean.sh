#!/bin/bash
DIR="$( cd -P "$( dirname "$0" )" && pwd )";

echo "Cleaning inside DIR: $DIR" 
#"$DIR/src"

/bin/rm -rf  \
    "$DIR/target/classes" \
    "$DIR/target/generated-sources"  \
    "$DIR/target/original-WsnUSBCollect-1.0-SNAPSHOT.jar"  \
    "$DIR/target/WsnUSBCollect-1.0-SNAPSHOT.jar"




