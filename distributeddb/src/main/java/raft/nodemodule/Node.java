package raft.nodemodule;

import raft.LifeCycle;
import raft.consensusmodule.*;
import raft.logmodule.RaftLogModule;
import raft.rpcmodule.RaftRpcServer;
import raft.statemachinemodule.RaftStateMachine;

public class Node implements LifeCycle {

    private RaftRpcServer rpcServer;
    private RaftConsensus consensus;
    private RaftLogModule logModule;
    private RaftStateMachine stateMachine;

    public void setConfig() {

    }

    public RequestVoteResult handleRequestVote(RequestVoteArgs args) {
        return null;
    }

    public AppendEntriesResult handleAppendEntries(AppendEntriesArgs args) {
        return null;
    }

    public ClientResponse handleClientRequest(ClientRequest req) {
        return null;
    }

    // redirect to leader
    public ClientResponse redirect(ClientRequest req) {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}
