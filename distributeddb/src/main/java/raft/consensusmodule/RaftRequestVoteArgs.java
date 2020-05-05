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
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public String toString() {
        return String.format("RequestVoteArgs term: %s, candidatesId:%s lastLogIndex:%s, lastLogTerm:%s",
                this.term, this.candidateId, this.lastLogIndex, this.lastLogTerm);
    }
}
