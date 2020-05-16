package raft.nodemodule;

import raft.statemachinemodule.RaftCommand;

public class RaftClientResponse {
    public final RaftCommand command;
    public final String key;
    public final String result;

    public RaftClientResponse(RaftCommand command, String key, String result) {
        this.command = command;
        this.key = key;
        this.result = result;
    }
}
