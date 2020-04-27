package raft;

import raft.logmodule.LogEntry;

public interface LogModule {
    void write(LogEntry logEntry);

    LogEntry read(Long index);

    void removeOnStartIndex(Long startIndex);

    LogEntry getLast();

    Long getLastIndex();
}
