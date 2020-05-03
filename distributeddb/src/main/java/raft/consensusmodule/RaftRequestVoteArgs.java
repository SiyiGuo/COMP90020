package raft.consensusmodule;

import raft.logmodule.LogEntry;

public class RaftRequestVoteArgs {
    public final long term;
    public final int candidateId;
    public final long lastLogIndex;
    public final LogEntry lastLogTerm;

    /*
    Proxy instance used by algorithm
     */
    public RaftRequestVoteArgs(long term, int candidateId, long lastLogIndex, LogEntry lastLogTerm) {
        System.out.println("Create args");
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
}
