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
        return this.logs.get(Math.toIntExact(index));
    }

    @Override
    public void removeOnStartIndex(Long startIndex) {

    }

    @Override
    public LogEntry getLast() {
//        if (this.logs.size() > 0) {
//            return this.logs.get(this.logs.size()-1);
//        }
        return new LogEntry(-1);
    }

    @Override
    public Long getLastIndex() {
        // index from one
//        if (this.logs.size() > 0) {
//            return (long)(this.logs.size()-1);
//        }
        return (long)0;
    }
}
