package raft.consensusmodule;

public class RequestVoteResult {
    public final long term;
    public final boolean voteGranted;

    public RequestVoteResult(long term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}
