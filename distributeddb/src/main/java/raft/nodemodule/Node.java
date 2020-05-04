package raft.nodemodule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.LifeCycle;
import raft.LogModule;
import raft.concurrentutil.RaftThreadPool;
import raft.consensusmodule.*;
import raft.logmodule.RaftLogModule;
import raft.rpcmodule.RequestVoteClient;
import raft.rpcmodule.RequestVoteServer;
import raft.statemachinemodule.RaftState;
import raft.statemachinemodule.RaftStateMachine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

public class Node implements LifeCycle, Runnable{
    public final static Logger logger = LogManager.getLogger(Node.class);

    public final int nodeId;
    private  RaftConsensus consensus;
    private  RaftLogModule logModule;
    private  RaftStateMachine stateMachine;

    //state of this node
    private volatile RaftState state;

    //Persistent state on all servers
    private volatile long currentTerm;
    private volatile int votedFor; // candidate Id that received vote in a current term

    // volatile state on all servers
    private volatile long commitIndex; //highest log entry known to be commited
    private volatile long lastApplied;

    // volatile state on leaders
    // reinitialized after election
    private volatile ArrayList<Integer> nextIndex;
    private volatile ArrayList<Integer> matchIndex;

    // time variable
    private volatile long lastHeartBeatTime = 0;
    private volatile long lastElectionTime = 0;

    // Task
    private HeartBeatTask heartBeatTask ;
    private ElectionTask electionTask;

    /* Engineering Variables*/
    // config for this node
    public final NodeConfig config;
    private RaftThreadPool threadPool;

    /* RPC related*/
    private RequestVoteServer rpcServer;
    // Peers
    public ArrayList<RequestVoteClient> peers;
    public int rpcCount;

    // Other
    private volatile boolean started;

    public Node(NodeConfig config) {
        this.nodeId = config.listenPort;

        this.commitIndex = 0;
        this.lastApplied = 0;
        this.state = RaftState.FOLLOWER;
        this.config = config;
        this.rpcCount = 0;

        this.heartBeatTask = new HeartBeatTask();
        this.electionTask = new ElectionTask();
    }

    public RaftState getState() {
        return this.state;
    }

    public RaftRequestVoteResult handleRequestVote(RaftRequestVoteArgs args) {
        return new RaftRequestVoteResult(
                this.currentTerm,
                false
        );
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

    @Override
    public void init() {
        if (started) return;

        /*
        Run the initilization of the server
         */
        // create peer list
        this.peers = new ArrayList<>();
        for(NodeConfig.NodeAddress peer: this.config.peers) {
            this.peers.add(new RequestVoteClient(peer.hostname, peer.port));
        }
        this.threadPool = new RaftThreadPool(this.nodeId);

    }

    public void startNodeRunning() {
        /*
        Actual initial sequence
         */
        // run rpc server
        this.rpcServer = new RequestVoteServer(this.config.listenPort, this);
        this.threadPool.execute(rpcServer);
        this.threadPool.scheduleWithFixedDelay(heartBeatTask, NodeConfig.TASK_DELAY);
        this.threadPool.scheduleAtFixedRate(electionTask, 6000, NodeConfig.TASK_DELAY);

        this.logModule = new RaftLogModule();
        this.consensus = new RaftConsensus();
        this.stateMachine = new RaftStateMachine();
    }

    @Override
    public void destroy() {
        if (this.rpcServer != null) {
            this.rpcServer.stop();
        }
    }

    @Override
    public void run() {
        // called by new Thread
        this.init();
        this.startNodeRunning();
    }

    class ElectionTask implements Runnable {
        // nested class such that can use Node's private variable
        @Override
        public void run() {
            // look into time stamp
            if (state  == RaftState.LEADER) {
                return;
            }

            // wait for a random amount of variable
            long currentTime = System.currentTimeMillis();
            long electionTime = NodeConfig.ELECTION_INTERVAL_MS + ThreadLocalRandom.current().nextLong(50);
            if (currentTime - lastElectionTime < electionTime) {
                return;
            }

            /*
            start election.
            */
            logger.info("node {} start election task", nodeId);
            lastElectionTime = currentTime+ThreadLocalRandom.current().nextLong(200)+150;

            currentTerm += 1; // increments its current term
            state = RaftState.CANDIDATE; // transition sot candidate state
            votedFor = nodeId; // vote for itself


            //RequestVoteRPC in parallel to other server
            ArrayList<Future> results = new ArrayList<>();
            for(RequestVoteClient peer:peers) {
                results.add(threadPool.submit(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        RaftRequestVoteArgs request = new RaftRequestVoteArgs(currentTerm, nodeId, logModule.getLastIndex(), logModule.getLast());
                        return peer.requestVote(request);
                    }
                }));
            }
        }
    }

    class HeartBeatTask implements Runnable {
        @Override
        public void run() {
            if (state != RaftState.LEADER) {
                // do some thing as loeader
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastHeartBeatTime < NodeConfig.HEARTBEAT_INTERVAL_MS) {
                return;
            }
        }
        // nested class so that we can use Node's private variable
    }
}
