package raft.logmodule;

public class LogEntry {
    public final long term;
    public LogEntry(long term) {
        this.term = term;
    }
}
