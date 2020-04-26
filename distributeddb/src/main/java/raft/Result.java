package raft;

public class Result {
    public final int term;
    public final boolean success; // AKA voteGranted

    public Result(int term, boolean success) {
        this.term = term;
        this.success = success;
    }
}
