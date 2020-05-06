package raft;

import raft.logmodule.RaftLogEntry;

public interface StateMachine {
    void apply(RaftLogEntry raftLogEntry);


    RaftLogEntry get(String key);

    String getString(String key);

    void setString(String key, String value);

    void delString(String... key);
}
