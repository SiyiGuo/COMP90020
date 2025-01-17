package application.storage;

import raft.logmodule.RaftLogEntry;

public interface LogStorage {
    void add(long timestamp, RaftLogEntry logEntry, int leaderId);
    void removeOnStartIndex(long timestamp, Long startIndex);
    void setLogName(String nodeId);
}
