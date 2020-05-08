package raft.consensusmodule;

public class RaftAppendEntriesResult {
    public final long term;
    public final boolean success;

    /*
    Proxy instance used by algorithm
     */
    public RaftAppendEntriesResult(long term, boolean success) {
        this.term = term;
        this.success = success;
    }
}
