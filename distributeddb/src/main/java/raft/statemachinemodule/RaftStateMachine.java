package raft.statemachinemodule;

import raft.StateMachine;
import raft.logmodule.LogEntry;

public class RaftStateMachine implements StateMachine {
    @Override
    public void apply(LogEntry logEntry) {

    }

    @Override
    public LogEntry get(String key) {
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
