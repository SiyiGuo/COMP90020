package raft.nodemodule;

import raft.statemachinemodule.RaftCommand;

public class RaftClientRequest {
    public final RaftCommand command;
    public final String key;
    public final String value;

    public RaftClientRequest(RaftCommand command, String key, String value) {
        this.command = command;
        this.key = key;
        this.value = value;
    }
}
