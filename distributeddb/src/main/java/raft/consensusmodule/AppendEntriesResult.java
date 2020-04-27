package raft.consensusmodule;

public class AppendEntriesResult {
    public final long term;
    public final boolean success;

    public AppendEntriesResult(long term, boolean success) {
        this.term = term;
        this.success = success;
    }
}
