package raft.consensusmodule;

import raft.logmodule.RaftLogEntry;

import java.util.List;

public class RaftAppendEntriesArgs {
    public final long term;
    public final int leaderId;
    public final long prevLogIndex;
    public final long prevLogTerm;
    public final List<RaftLogEntry> entries;
    public final long leaderCommit;

    /*
    Proxy instance used by algorithm
     */
    public RaftAppendEntriesArgs(long term, int leaderId, long prevLogIndex,
                                 long prevLogTerm, List<RaftLogEntry> entries, long leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
    }

    @Override
    public String toString() {
        String result =  String.format(
                "term:%s leaderId:%s prevLogIndex:%s prevLogTerm:%s leaderCommit:%s",
                term, leaderId, prevLogIndex, prevLogTerm, leaderCommit
        );
        result += "\nEntries:\n";
        for(RaftLogEntry e: entries) {
            result += (e+"\n");
        }
        return result;
    }
}
