package raft.statemachinemodule;

import raft.LogModule;
import raft.StateMachine;
import raft.logmodule.LogEntry;
import raft.logmodule.RaftLogModule;

import java.util.ArrayList;

public class RaftStateMachine implements StateMachine {
    public RaftStateMachine() {
    }


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
