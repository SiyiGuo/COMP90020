package raft.nodemodule;

import raft.consensusmodule.*;
import raft.logmodule.RaftLogModule;
import raft.rpcmodule.RaftRpcServer;
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

    public ClientResponse handleClientRequest(ClientRequest req) {

    }

    // redirect to leader
    public ClientResponse redirect(ClientRequest req) {

    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}
