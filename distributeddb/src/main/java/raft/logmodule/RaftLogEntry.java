package raft.logmodule;

public class RaftLogEntry {
    public final long term;
    public final String value;

    public RaftLogEntry(long term, String value) {
        this.term = term;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("LogEntry.term:%s value:%s",
                this.term, this.value);
    }
}
