package raft;

import raft.logmodule.RaftLogEntry;

public interface LogModule {
    void append(RaftLogEntry raftLogEntry);

    RaftLogEntry read(Long index);

    void removeOnStartIndex(Long startIndex);

    RaftLogEntry getLast();

    Long getLastIndex();
}
