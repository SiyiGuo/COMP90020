package raft.consensusmodule;

import raft.logmodule.LogEntry;

public class RequestVoteArgs {
    public final long term;
    public final int candidateId;
    public final long lastLogIndex;
    public final LogEntry lastLogTerm;

    public RequestVoteArgs(long term, int candidateId, long lastLogIndex, LogEntry lastLogTerm) {
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
}
