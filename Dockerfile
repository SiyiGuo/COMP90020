FROM openjdk:8

ADD ./distributeddb/target/original-distributeddb-1.0-SNAPSHOT.jar /raft.jar

ENTRYPOINT ["java", "-jar", "/raft.jar"]