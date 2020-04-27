package raft.consensusmodule;

import raft.logmodule.LogEntry;

public class AppendEntriesArgs {
    public final long term;
    public final int leaderId;
    public final long prevLogIndex;
    public final LogEntry prevLogTerm;
    public final LogEntry[] entries;
    public final long leaderCommit;

    public AppendEntriesArgs(long term, int leaderId, long prevLogIndex,
                             LogEntry prevLogTerm, LogEntry[] entries, long leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
    }
}
