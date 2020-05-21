package raft.statemachinemodule;

import application.storage.Storage;
import raft.StateMachine;
import raft.concurrentutil.Cu;
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
        String key = raftLogEntry.key;
        String value = raftLogEntry.value;
        switch (raftLogEntry.command) {
            case GET:
                break;
            case PUT:
                this.setString(key, value);
                break;
            case DELETE:
                this.delString(key);
                break;
            case UPDATE:
                this.setString(key, value);
                break;
        }
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
        try {
            return this.stroage.get(key);
        } catch (Exception e) {
            System.err.println("******************");
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
            return "Key not present";
        }
    }

    @Override
    public void setString(String key, String value) {
        this.stroage.put(key, value);
    }

    @Override
    public void delString(String... keys) {
        for(String key:keys) {
            this.stroage.delete(key);
        }
    }

    public Storage getStorage() {
        return stroage;
    }
}
