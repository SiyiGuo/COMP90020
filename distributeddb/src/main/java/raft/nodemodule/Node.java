package raft.nodemodule;

import raft.LifeCycle;
import raft.LogModule;
import raft.consensusmodule.*;
import raft.logmodule.RaftLogModule;
import raft.rpcmodule.RaftRpcServer;
import raft.statemachinemodule.RaftState;
import raft.statemachinemodule.RaftStateMachine;

import java.util.ArrayList;

public class Node implements LifeCycle {

    private RaftRpcServer rpcServer;
    private RaftConsensus consensus;
    private RaftLogModule logModule;
    private RaftStateMachine stateMachine;

    //state of this node
    private RaftState state;

    //Persistent state on all servers
    private long currentTerm;
    private int votedFor; // candidate IT that received vote in a current term

    // volatile state on all servers
    private long commitIndex; //highest log entry known to be commited
    private long lastApplied;

    // volatile state on leaders
    // reinitialized after election
    private ArrayList<Integer> nextIndex;
    private ArrayList<Integer> matchIndex;

    public Node() {
        this.commitIndex = 0;
        this.lastApplied = 0;
        this.state = RaftState.FOLLOWER;
    }

    public void election() {
        currentTerm += 1;
        this.state = RaftState.CANDIDATE;

    }

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
