package raft.statemachinemodule;

import application.storage.Storage;
import raft.StateMachine;
import raft.logmodule.RaftLogEntry;

/*
This keep state modified by algorithm and LogEntry
 */
public class RaftStateMachine implements StateMachine {
    private Storage stroage;
    public RaftStateMachine(Storage storage) {
        this.stroage = storage;
    }


    @Override
    public void apply(RaftLogEntry raftLogEntry) {

    }

    @Override
    public RaftLogEntry get(String key) {
        /*
        TODO: why we need to have a get for log entry in databse
        if state machine only store the value
         */
        return null;
    }

    @Override
    public String getString(String key) {
        return this.stroage.get(key);
    }

    @Override
    public void setString(String key, String value) {
        boolean result = this.stroage.put(key, value);
    }

    @Override
    public void delString(String... keys) {
        for(String key:keys) {
            this.stroage.delete(key);
        }
    }
}
