package raft.logmodule;

public class LogEntry {
    public final long term;
    public LogEntry(long term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return String.format("LogEntry.term:%s", this.term);
    }
}
