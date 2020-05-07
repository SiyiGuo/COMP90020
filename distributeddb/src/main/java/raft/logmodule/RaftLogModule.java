package raft.logmodule;

import raft.LogModule;

import java.util.ArrayList;

public class RaftLogModule implements LogModule {
    private ArrayList<RaftLogEntry> logs;

    public RaftLogModule() {
        this.logs = new ArrayList<>();
    }

    @Override
    public void append(RaftLogEntry raftLogEntry) {
        this.logs.add(raftLogEntry);
        //TODO: write to file?
    }

    @Override
    public RaftLogEntry getLog(Long index) {
        return this.logs.get(Math.toIntExact(index));
    }

    @Override
    public void removeOnStartIndex(Long startIndex) {

    }

    @Override
    public RaftLogEntry getLast() {
        if (this.logs.size() > 0) {
            return this.logs.get(this.logs.size()-1);
        }
        return new RaftLogEntry(-1, "");
    }

    @Override
    public Long getLastIndex() {
        // index from one
        if (this.logs.size() > 0) {
            return (long)(this.logs.size()-1);
        }
        return (long) 0;
    }
}
