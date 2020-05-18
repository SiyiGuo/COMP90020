package application.storage;

import raft.logmodule.RaftLogEntry;

public interface LogStorage {
    boolean add(long timestamp, RaftLogEntry logEntry);
    boolean removeOnStartIndex(long timestamp, Long startIndex);
}
