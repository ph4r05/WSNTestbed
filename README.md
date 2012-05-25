WSNTestbed
==========


## Overview==

Java command line application for manipulating with Wireless Sensor Network (WSN), collecting data
and controlling running experiment on testbed.

## Warning!
Technical documentation for this project is in early stages, this is only short
description. There can be some inaccuracies in this text.

Application has been made for specific needs of our laboratory at Masaryk University,
thus it contains some very specific features/settings. But it also contains some 
universal classes which can be useful for others. 

## Requirements
Requires modified version of TinyOS Java SDK, see TinyOSJava-2.1.ph4edit repo.
In order to build this application you need to install aforementioned package
to maven repository. This can be done by following command:

    mvn install:install-file -Dfile=./WSNTinyOS.jar -DgroupId=net -DartifactId=tinyos -Dversion=2.1ph4 -Dpackaging=jar

assuming that built TinyOSJava-2.1.ph4edit is called WSNTinyOS.jar.

Next you will need new jython. You can obtain it from: [[http://sourceforge.net/projects/jython/files/jython-dev/2.5.3b1/jython_installer-2.5.3b1.jar/download]]

    mvn install:install-file -Dfile=./jython-2.5.3b1.jar -DgroupId=org.python -DartifactId=jython -Dversion=2.5.3b1 -Dpackaging=jar

Building this application is done by:

    mvn package

## Main technologies used 
* Spring as dependency injection container.
* Hibernate for DB connection, ORM.
* c3p0 database connection pool
* Jython for interactive shell.
* SLF4J for logging


## Description
This application provides framework for WSN for server side application. One can easy 
send messages synchronously or asynchronously, register as packet listener. Level of
abstraction is added compared to original TinyOS Java SDK to support connecting/disconnecting
to/from nodes without any need to re-register in high level, this maintenance is handled
in low layer code. 

When application starts from command line it provides Jython shell - python implementation
in pure java. One can write python scripts in it and use java classes. Thus one can
control application behavior from this shell by starting/stopping experiment, 
monitor application state, initialize new TinyOS message and send it to node and so on.
In short one can interact with WSN testbed interactively. 

Message listeners has its own queues which buffers incoming messages not to loose any
because of high load. Message sender has queue of messages to send.

With application one can analyze incoming messages from testbed and store results
to database easily with defined ORM mapping or to CSV/XML files. 

Application contains simple statistical tools to compute statistics such as mean, 
median, standard deviation, 1st, 3rd quartile, min, max. This is useful for analysis
of collected data. 

Application can be used as serial forwarder for multiple connected nodes. One can 
start serial forwarders for example for 200 nodes. This way is easy to communicate
with whole testbed via IP network. Packets received on this application uses 
timestamping features added to TinyOSJava-2.1.ph4edit so client connected to 
application's serial forwarder has accurate time of message reception directly 
on serial interface.

One can test throughput of database system with prepared benchmark modules.

Application is compatible with classical PrintF messages, it is possible to log them
to the log file and to the database. 

Author: Dusan Klinec (ph4r05)