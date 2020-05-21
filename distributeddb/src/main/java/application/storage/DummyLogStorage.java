package application.storage;

import raft.logmodule.RaftLogEntry;

public class DummyLogStorage implements LogStorage{

    @Override
    public void add(long timestamp, RaftLogEntry logEntry, int leaderId) {
        ;
    }

    @Override
    public void removeOnStartIndex(long timestamp, Long startIndex) {
        ;
    }

    @Override
    public void setLogName(String nodeId){
        ;
    }
}
