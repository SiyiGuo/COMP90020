package raft.logmodule;

public class RaftLogEntry {
    public final long term;
    public final String value;
    public final long index;

    public RaftLogEntry(long term, long index, String value) {
        this.term = term;
        this.index = index;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("LogEntry.term:%s index:%s value:%s",
                this.term, this.index, this.value);
    }
}
