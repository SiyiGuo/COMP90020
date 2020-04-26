package raft;

public class RaftState {
    public final int term;
    public final boolean success; // AKA voteGranted

    public RaftState(int term, boolean success) {
        this.term = term;
        this.success = success;
    }
}
