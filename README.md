# README

### Introduction ###
This is the project for COMP90020 Distributed Algorithm. We implement the Raft algorithm and having a command line tool to interact with it, as well as a front end package to visualize node log.

At this implementation, we set to be a fix of 3 node cluster. 

### Project Requirement ###
Oracle Java 8

Maven

### File Structure ###
distributeddb: package contains the raft algorithm implementation and some backend application code

frontend: the frontend application for visualization

pythonproxy: proxy to communicate between frontend and backend

report: placehold for report related document.

Dockerfile, docker-compose, start.sh: Help launching the project.

### Design Document ###
The program architecture is described as below

Taken from: https://github.com/stateIs0/lu-raft-kv

![Architecture](./design.png)

### To Use the Application ###
The application can run natively or run in the docker.

#### before running the application ####
Install all the dependency and generate the grpc related files
```
cd distributeddb
mvn install 
```

Create a jar
```
cd distributeddb
mvn package
```

#### To run without docker ####
Command Syntax
```
java -jar $(path to package) -m (peer/controller) -p (Peer only, only 8258, 8259, 8260)
```

Example Command to run node:
```shell script
cd distributeddb/target
java -jar .\original-distributeddb-1.0-SNAPSHOT.jar -m peer -p 8258 -h localhost
java -jar .\original-distributeddb-1.0-SNAPSHOT.jar -m peer -p 8259 -h localhost
java -jar .\original-distributeddb-1.0-SNAPSHOT.jar -m peer -p 8260 -h localhost
```

Example Command to run controller
```shell script
cd distributeddb/target
java -jar .\original-distributeddb-1.0-SNAPSHOT.jar -m controller -h localhost
```

#### To run with docker ####
Make sure you follow before running the application first

0. cd the root directory of the project

1. build image
```
docker build -t raft .
```

3. run peers using docker compose
```
docker-compose up -d
```

4. run controller to connect them
```
cd distributeddb/target
java -jar .\original-distributeddb-1.0-SNAPSHOT.jar -m peer -p 8260 -h localhost
```

### Contributor ###
Siyi Guo, 737008

Junlin chen, 1065399

Jihai Fan, 832919

Mofan Li, 741567