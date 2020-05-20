FROM openjdk:8

ADD ./distributeddb/target/original-distributeddb-1.0-SNAPSHOT.jar /raft.jar
EXPOSE 8258/tcp
EXPOSE 8259/tcp
EXPOSE 8260/tcp
ENTRYPOINT ["java", "-jar", "/raft.jar"]