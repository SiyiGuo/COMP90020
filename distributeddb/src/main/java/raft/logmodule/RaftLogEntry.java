package raft.logmodule;

import raft.statemachinemodule.RaftCommand;

public class RaftLogEntry {
    public final long term;
    public final String value;
    public final RaftCommand command;
    public final long index;

    public RaftLogEntry(long term, long index, RaftCommand command, String value) {
        this.term = term;
        this.index = index;
        this.command = command;
        this.value = value; // format: command:new_value
    }

    @Override
    public String toString() {
        return String.format("LogEntry.term:%s index:%s command: %s, value:%s",
                this.term, this.index, command, this.value);
    }
}
