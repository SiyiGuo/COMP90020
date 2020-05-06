package raft.consensusmodule;

import raft.logmodule.RaftLogEntry;

public class RaftAppendEntriesArgs {
    public final long term;
    public final int leaderId;
    public final long prevLogIndex;
    public final RaftLogEntry prevLogTerm;
    public final RaftLogEntry[] entries;
    public final long leaderCommit;

    /*
    Proxy instance used by algorithm
     */
    public RaftAppendEntriesArgs(long term, int leaderId, long prevLogIndex,
                                 RaftLogEntry prevLogTerm, RaftLogEntry[] entries, long leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
    }
}
