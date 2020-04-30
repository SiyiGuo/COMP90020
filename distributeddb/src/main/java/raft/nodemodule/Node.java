package raft.nodemodule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.LifeCycle;
import raft.consensusmodule.*;
import raft.logmodule.RaftLogModule;
import raft.rpcmodule.RequestVoteClient;
import raft.rpcmodule.RequestVoteServer;
import raft.statemachinemodule.RaftState;
import raft.statemachinemodule.RaftStateMachine;

import java.io.IOException;
import java.util.ArrayList;

public class Node implements LifeCycle, Runnable{
    public final static Logger logger = LogManager.getLogger(Node.class);

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

    // config for this node
    public final NodeConfig config;

    /*
    RPC related
     */
    private RequestVoteServer rpcServer;
    // Peers
    public ArrayList<RequestVoteClient> peers;
    public int rpcCount;

    public Node(NodeConfig config) {
        this.commitIndex = 0;
        this.lastApplied = 0;
        this.state = RaftState.FOLLOWER;
        this.config = config;
        this.rpcCount = 0;
    }

    public void election() {
        currentTerm += 1;
        this.state = RaftState.CANDIDATE;
    }

    public RaftState getState() {
        return this.state;
    }

    public RaftRequestVoteResult handleRequestVote(RaftRequestVoteArgs args) {
        return null;
    }

    public RaftAppendEntriesResult handleAppendEntries(RaftAppendEntriesArgs args) {
        return null;
    }

    public ClientResponse handleClientRequest(ClientRequest req) {
        return null;
    }

    // redirect to leader
    public ClientResponse redirect(ClientRequest req) {
        return null;
    }

    public void startNodeRunning() {
        /*
        Actual initial sequence
         */
    }

    @Override
    public void init() {
        /*
        Run the initilization of the server
         */
        // run rpc server
        this.rpcServer = new RequestVoteServer(this.config.listenPort, this);
        new Thread(this.rpcServer).start();

        // create peer list
        this.peers = new ArrayList<>();
        for(NodeConfig.NodeAddress peer: this.config.peers) {
            this.peers.add(new RequestVoteClient(peer.hostname, peer.port));
        }
    }

    @Override
    public void destroy() {
       this.rpcServer.stop();
    }

    @Override
    public void run() {
        // called by new Thread
        this.init();
        this.startNodeRunning();
    }
}
