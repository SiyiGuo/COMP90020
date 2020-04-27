package raft.logmodule;

import raft.LogModule;

import java.util.ArrayList;

public class RaftLogModule implements LogModule {
    private ArrayList<LogEntry> logs;

    public RaftLogModule() {
        this.logs = new ArrayList<>();
    }

    @Override
    public void append(LogEntry logEntry) {
        this.logs.add(logEntry);
        //TODO: write to file?
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
