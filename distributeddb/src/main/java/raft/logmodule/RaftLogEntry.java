package raft.logmodule;
import org.json.simple.JSONObject;
import raft.statemachinemodule.RaftCommand;

public class RaftLogEntry {
    public final long term;
    public final String key;
    public final String value;
    public final RaftCommand command;
    public final long index;

    public RaftLogEntry(long term, long index, RaftCommand command, String key, String value) {
        this.term = term;
        this.index = index;
        this.command = command;
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("LogEntry.term:%s index:%s command: %s, key:%s, value:%s",
                this.term, this.index, this.command, this.key, this.value);
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("term", term);
        obj.put("key", key);
        obj.put("value", value);
        obj.put("command", command.name());
        obj.put("index", index);
        return obj;
    }
}
