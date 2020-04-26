package raft;

import raft.consensusmodule.*;
import raft.logmodule.RaftLogModule;
import raft.statemachinemodule.RaftStateMachine;

public class RaftNode implements RaftLifeCycle {

    private RaftRpcServer rpcServer;
    private RaftConsensus consensus;
    private RaftLogModule logModule;
    private RaftStateMachine stateMachine;

    public void setConfig() {

    }

    public RequestVoteResult handleRequestVote(RequestVoteArgs args) {

    }

    public AppendEntriesResult handleAppendEntries(AppendEntriesArgs args) {

    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}
