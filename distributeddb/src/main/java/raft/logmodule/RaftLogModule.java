package raft.logmodule;

import raft.LogModule;

public class RaftLogModule implements LogModule {
    @Override
    public void write(LogEntry logEntry) {

    }

    @Override
    public LogEntry read(Long index) {
        return null;
    }

    @Override
    public void removeOnStartIndex(Long startIndex) {

    }

    @Override
    public LogEntry getLast() {
        return null;
    }

    @Override
    public Long getLastIndex() {
        return null;
    }
}
