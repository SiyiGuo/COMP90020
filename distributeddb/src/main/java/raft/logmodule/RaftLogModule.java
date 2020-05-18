package raft.logmodule;

import application.storage.LogStorage;
import application.storage.Storage;
import raft.LogModule;
import raft.statemachinemodule.RaftCommand;

import java.util.ArrayList;
import java.util.List;

public class RaftLogModule implements LogModule {
    private ArrayList<RaftLogEntry> logs;
    private LogStorage storage;

    public RaftLogModule(LogStorage storage) {
        this.storage = storage;
        this.logs = new ArrayList<>();
    }

    @Override
    public void append(RaftLogEntry raftLogEntry) {
        this.logs.add(raftLogEntry);
        this.storage.add(System.currentTimeMillis(), raftLogEntry);
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
        this.storage.removeOnStartIndex(System.currentTimeMillis(), startIndex);
    }

    // [startIndex, endIndex) inclusive, exclusive
    public List<RaftLogEntry> getLogsOnStartIndex(Long startIndex) {
        // as in algorithm, index is start from 1
        // convert back to program implementation, we need to -1 to access right item
        return this.logs.subList(Math.toIntExact(startIndex)-1, this.logs.size());
    }

    @Override
    public RaftLogEntry getLast() {
        if (this.logs.size() > 0) {
            return this.logs.get(this.logs.size()-1);
        }
        /*
        TODO
        check assumption here
        we assume is there is no log, return term0, index0 and no value
         */
        return new RaftLogEntry(0, 0, RaftCommand.GET, "", "");
    }

    @Override
    public Long getLastIndex() {
        // index from one
        if (this.logs.size() > 0) {
            return (long)(this.logs.size());
        }
        return (long) 0;
    }

    public ArrayList<RaftLogEntry> getAllLogs() {
        return this.logs;
    }
}
