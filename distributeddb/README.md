### Project Requirement ###
Oracle Java 8

Run mvn clean to remove things

Run Mavn Install to generate protobuf classes.

Then run the test. 

### Design Document ###
The program architecture is described as below

Taken from: https://github.com/stateIs0/lu-raft-kv

![Architecture](./design.png)

### TO Use the Application
java -jar $(package name) -m (peer/controller) -p (Peer only, only 8258, 8259, 8260)