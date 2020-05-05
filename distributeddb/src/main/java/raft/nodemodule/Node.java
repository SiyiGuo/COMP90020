package raft.nodemodule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.LifeCycle;
import raft.concurrentutil.RaftThreadPool;
import raft.consensusmodule.*;
import raft.logmodule.RaftLogModule;
import raft.rpcmodule.RaftRpcClient;
import raft.rpcmodule.RaftRpcServer;
import raft.statemachinemodule.RaftState;
import raft.statemachinemodule.RaftStateMachine;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
    public final static int NULL_VOTE = -1;
    private volatile int votedFor = NULL_VOTE; // candidate Id that received vote in a current term

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
    private RaftRpcServer rpcServer;
    // Peers
    public ArrayList<RaftRpcClient> peers;
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

    @Override
    public void init() {
        if (started) return;

        /*
        Run the initilization of the server
         */
        // create peer list
        this.peers = new ArrayList<>();
        for(NodeConfig.NodeAddress peer: this.config.peers) {
            this.peers.add(new RaftRpcClient(peer.hostname, peer.port));
        }
        this.threadPool = new RaftThreadPool(this.nodeId);

    }

    public void startNodeRunning() {
        /*
        Actual initial sequence
         */
        // run rpc server
        this.rpcServer = new RaftRpcServer(this.config.listenPort, this);
        this.threadPool.execute(rpcServer);
        this.threadPool.scheduleWithFixedDelay(heartBeatTask, NodeConfig.TASK_DELAY);
        this.threadPool.scheduleAtFixedRate(electionTask, 6000, NodeConfig.TASK_DELAY);

        // start 3 module
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

    public RaftState getState() {
        return this.state;
    }

    public RaftRequestVoteResult handleRequestVote(RaftRequestVoteArgs args) {
        logger.info("Node: {} receive election request: {} currentTerm {} votedFor {}", this.nodeId, args, currentTerm, votedFor);
        if (args.term < this.currentTerm) {
            return new RaftRequestVoteResult(
                    this.currentTerm,
                    false
            );
        }

        if (votedFor == NULL_VOTE || votedFor == args.candidateId) {
            return new RaftRequestVoteResult(
                    this.currentTerm,
                    true
            );
        }

        return new RaftRequestVoteResult(this.currentTerm, false);
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

    class ElectionTask implements Runnable {
        // nested class such that can use Node's private variable
        @Override
        public void run() {
            // look into time stamp
            if (state  == RaftState.LEADER) return;

            // wait for a random amount of variable
            long currentTime = System.currentTimeMillis();
            long electionTime = NodeConfig.ELECTION_INTERVAL_MS + ThreadLocalRandom.current().nextLong(350, 500);
            if (currentTime - lastElectionTime < electionTime) return;

            /*
           start election.
            */
            logger.info("node {} start election task", nodeId);
            lastElectionTime = currentTime+ThreadLocalRandom.current().nextLong(200)+150;

            currentTerm += 1; // increments its current term
            state = RaftState.CANDIDATE; // transition sot candidate state
            votedFor = nodeId; // vote for itself

            //RequestVoteRPC in parallel to other peers
            ArrayList<Future> results = new ArrayList<>();
            for(RaftRpcClient peer:peers) {
                results.add(threadPool.submit(() -> {
                    try {
                        RaftRequestVoteArgs request = new RaftRequestVoteArgs(currentTerm, nodeId, logModule.getLastIndex(), logModule.getLast());
                        return peer.requestVote(request);
                    } catch(Exception e) {
                        return null;
                    }
                }));
            }

            // receive RequestVoteRpc Result
            AtomicInteger receivedVote = new AtomicInteger(0);
            CountDownLatch countDown = new CountDownLatch(results.size());
            for(Future peerResult: results) {
                threadPool.submit(()->{
                    try{
                        // All Servers: if RPC request or response contains term T > currentTerm, set currentTerm = T, convert to follower
                        if (state == RaftState.FOLLOWER) return -1;
                        RaftRequestVoteResult result = (RaftRequestVoteResult)peerResult.get(NodeConfig.RPC_RESULT_WAIT_TIME, MILLISECONDS);
                        logger.info("election task received result: term {} voteGranted {}", result.term, result.voteGranted);
                        if (result == null) {
                            return -1;
                        }

                        if (result.voteGranted) {
                            // count vote here
                            receivedVote.incrementAndGet();
                            System.out.println("election received vote1 " + receivedVote.intValue());
                        } else {
                            // All Servers: if RPC request or response contains term T > currentTerm, set currentTerm = T, convert to follower
                            if (result.term > currentTerm) {
                                currentTerm = result.term;
                                state = RaftState.FOLLOWER;
                            }
                        }
                        return 0;
                    } catch (Exception e) {
                      logger.error("Recieve Request Vote result fail, error: ", e);
                      return -1;
                    } finally {
                        countDown.countDown();
                    }
                });
            }

            try {
                // wait for the async result came back
                countDown.await(NodeConfig.RPC_RESULT_WAIT_TIME+500, MILLISECONDS);
            } catch (InterruptedException e) {
                logger.info("Node {} election task interrupted", nodeId);
            }

            // All servers: if RPC request or response contains term T > currentTerm, set currentTerm and convert to candidate.
            if (state == RaftState.FOLLOWER) {
                votedFor = NULL_VOTE;
                return;
            }

            // Candidate: if votes received from majority of servers, become leader
            System.out.println("election received vote " + receivedVote.intValue());
            if (receivedVote.intValue() > (peers.size() / 2)) {
                // 3 / 2 = 1, > 1 means 2, 3
                // 4 / 2 = 2, > 2 mean not majority
                state = RaftState.LEADER;
            }

            // Candidate: if election timeout elapses: start new election
            votedFor = NULL_VOTE;
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
