package raft.consensusmodule;

public class RaftRequestVoteResult {
    public final long term;
    public final boolean voteGranted;

    /*
    Proxy instance used by algorithm
     */
    public RaftRequestVoteResult(long term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}
