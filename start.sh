#!/bin/bash 
docker-compose up -d
mvn exec:java -Dexec.args="-m controller"