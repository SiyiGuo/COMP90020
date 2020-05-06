package raft.statemachinemodule;

import raft.StateMachine;
import raft.logmodule.RaftLogEntry;

public class RaftStateMachine implements StateMachine {
    public RaftStateMachine() {
    }


    @Override
    public void apply(RaftLogEntry raftLogEntry) {

    }

    @Override
    public RaftLogEntry get(String key) {
        return null;
    }

    @Override
    public String getString(String key) {
        return "";
    }

    @Override
    public void setString(String key, String value) {

    }

    @Override
    public void delString(String... key) {

    }
}
