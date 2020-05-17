package raft.logmodule;

import raft.LogModule;
import raft.statemachinemodule.RaftCommand;

import java.util.ArrayList;
import java.util.List;

public class RaftLogModule implements LogModule {
    private ArrayList<RaftLogEntry> logs;

    public RaftLogModule() {
        this.logs = new ArrayList<>();
    }

    @Override
    public void append(RaftLogEntry raftLogEntry) {
        this.logs.add(raftLogEntry);
    }

    @Override
    public RaftLogEntry getLog(Long index) {
        if (index >= this.logs.size()) {
            return null;
        }
        return this.logs.get(Math.toIntExact(index));
    }

    @Override
    public void removeOnStartIndex(Long startIndex) {
        // AppendEntries RPC
        // if an existing entry conflicts with a new one (same index but different terms)
        // delete the existing entry and all that follow it
        this.logs.subList(Math.toIntExact(startIndex), this.logs.size()).clear();
    }

    // [startIndex, endIndex) inclusive, exclusive
    public List<RaftLogEntry> getLogsOnStartIndex(Long startIndex) {
        return this.logs.subList(Math.toIntExact(startIndex), this.logs.size());
    }

    @Override
    public RaftLogEntry getLast() {
        if (this.logs.size() > 0) {
            return this.logs.get(this.logs.size()-1);
        }
        /*
        TODO
        check assumption here
        we aussme is there is no log, return term0, index0 and no value
         */
        return new RaftLogEntry(0, 0, RaftCommand.GET, "", "");
    }

    @Override
    public Long getLastIndex() {
        // index from one
        if (this.logs.size() > 0) {
            return (long)(this.logs.size()-1);
        }
        return (long) 0;
    }

    public ArrayList<RaftLogEntry> getAllLogs() {
        return this.logs;
    }
}
