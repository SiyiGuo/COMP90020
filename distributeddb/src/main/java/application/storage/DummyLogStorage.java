package application.storage;

import raft.logmodule.RaftLogEntry;

public class DummyLogStorage implements LogStorage{
    @Override
    public void add(long timestamp, RaftLogEntry logEntry) {
        ;
    }

    @Override
    public void removeOnStartIndex(long timestamp, Long startIndex) {
        ;
    }
}
