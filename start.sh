#!/bin/bash 
touch history_8258.json history_8259.json history_8260.json
docker-compose up -d
mvn exec:java -Dexec.args="-m controller"
## cd frontend/timeline && npm start