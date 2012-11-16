#!/bin/bash

# get current directory 
DIR="$( cd -P "$( dirname "$0" )" && pwd )"; 

cd $DIR

mvn clean install