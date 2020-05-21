package raft;

import raft.logmodule.RaftLogEntry;

public interface LogModule {
    void append(RaftLogEntry raftLogEntry, int leaderId);

    RaftLogEntry getLog(Long index);

    void removeOnStartIndex(Long startIndex);

    RaftLogEntry getLast();

    Long getLastIndex();
}
