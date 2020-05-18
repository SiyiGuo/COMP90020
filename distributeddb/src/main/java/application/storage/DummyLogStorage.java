package application.storage;

import raft.logmodule.RaftLogEntry;

public class DummyLogStorage implements LogStorage{
    @Override
    public boolean add(long timestamp, RaftLogEntry logEntry) {
        return false;
    }

    @Override
    public boolean removeOnStartIndex(long timestamp, Long startIndex) {
        return false;
    }
}
