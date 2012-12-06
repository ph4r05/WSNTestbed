#!/bin/bash

# get current directory 
DIR="$( cd -P "$( dirname "$0" )" && pwd )"; 

cd $DIR

# old version - installation, not necessary
# mvn clean install
mvn clean package

cd -
