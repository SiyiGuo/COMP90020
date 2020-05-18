# COMP90020

1. build image
```
docker build -t raft .
```

2. run peers using docker compose
```
docker compose up -d
```

3. run controller to connect them
```
mvn exec:java -Dexec.args="-m controller"
```