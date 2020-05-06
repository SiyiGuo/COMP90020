package raft.nodemodule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.LifeCycle;
import raft.concurrentutil.RaftThreadPool;
import raft.consensusmodule.RaftAppendEntriesArgs;
import raft.consensusmodule.RaftAppendEntriesResult;
import raft.consensusmodule.RaftConsensus;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.logmodule.RaftLogModule;
import raft.rpcmodule.RaftRpcClient;
import raft.rpcmodule.RaftRpcServer;
import raft.statemachinemodule.RaftState;
import raft.statemachinemodule.RaftStateMachine;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Node implements LifeCycle, Runnable {
    public final static Logger logger = LogManager.getLogger(Node.class);
    public final static int NULL_VOTE = -1;
    public final int nodeId;
    /* Engineering Variables*/
    // config for this node
    public final NodeConfig config;
    // Peers
    public ArrayList<RaftRpcClient> peers;
    public int rpcCount;
    private RaftConsensus consensus;
    private RaftLogModule logModule;
    private RaftStateMachine stateMachine;
    //state of this node
    private volatile RaftState state;
    //Persistent state on all servers
    private volatile long currentTerm;
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
    private volatile long timeOut = 0;
    // Task
    private HeartBeatTask heartBeatTask;
    private ElectionTask electionTask;
    private RaftThreadPool threadPool;
    /* RPC related*/
    private RaftRpcServer rpcServer;
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
        for (NodeConfig.NodeAddress peer : this.config.peers) {
            this.peers.add(new RaftRpcClient(peer.hostname, peer.port));
        }
        this.threadPool = new RaftThreadPool(Integer.toString(this.nodeId));
    }

    public void startNodeRunning() {
        /*
        Actual initial sequence
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

        // TODO: let consensus handle this
        if (args.term < this.currentTerm) {
            return new RaftRequestVoteResult(
                    this.currentTerm,
                    false
            );
        }

        if (votedFor == NULL_VOTE || votedFor == args.candidateId) {
            // TODO: and candidates's log is at least as up-to-date as receiver's log. grant vote
            this.state = RaftState.FOLLOWER;
            this.votedFor = args.candidateId;
            this.currentTerm = args.term;
            return new RaftRequestVoteResult(
                    this.currentTerm,
                    true
            );
        }

        return new RaftRequestVoteResult(this.currentTerm, false);
    }

    public RaftAppendEntriesResult handleAppendEntries(RaftAppendEntriesArgs args) {
        return new RaftAppendEntriesResult(this.currentTerm, false);
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
            if (state == RaftState.LEADER) return;

            // wait for a random amount of variable
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastElectionTime < timeOut) return;

            /*
           start election.
            */
            lastElectionTime = currentTime;
            timeOut = ThreadLocalRandom.current().nextLong(NodeConfig.ELECTION_TIMEOUT_RANGE) + NodeConfig.ELECTION_TIMEOUT_MIN;

            currentTerm += 1; // increments its current term
            state = RaftState.CANDIDATE; // transition sot candidate state
            votedFor = nodeId; // vote for itself

            //RequestVoteRPC in parallel to other peers
            ArrayList<Future> results = new ArrayList<>();
            for (RaftRpcClient peer : peers) {
                results.add(threadPool.submit(() -> {
                    try {
                        RaftRequestVoteArgs request = new RaftRequestVoteArgs(currentTerm, nodeId, logModule.getLastIndex(), logModule.getLast().term);
                        return peer.requestVote(request);
                    } catch (Exception e) {
                        return null;
                    }
                }));
            }

            // receive RequestVoteRpc Result
            AtomicInteger receivedVote = new AtomicInteger(0);
            CountDownLatch countDown = new CountDownLatch(results.size());
            for (Future peerResult : results) {
                threadPool.submit(() -> {
                    try {
                        // All Servers: if RPC request or response contains term T > currentTerm, set currentTerm = T, convert to follower
                        if (state == RaftState.FOLLOWER) return -1;
                        RaftRequestVoteResult result = (RaftRequestVoteResult) peerResult.get(NodeConfig.RPC_RESULT_WAIT_TIME, MILLISECONDS);
                        logger.info("election task received result: term {} voteGranted {}", result.term, result.voteGranted);
                        if (result == null) {
                            return -1;
                        }

                        // All Servers: if RPC request or response contains term T > currentTerm, set currentTerm = T, convert to follower
                        if (result.term > currentTerm) {
                            currentTerm = result.term;
                            state = RaftState.FOLLOWER;
                            return 0;
                        }

                        // collect vote result
                        if (result.voteGranted) {
                            receivedVote.incrementAndGet();
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
                // you need to wait for atleast ELECTION_TIMEOUT_MIN to collect result
                countDown.await(NodeConfig.ELECTION_TIMEOUT_MIN, MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("Node {} election task interrupted", nodeId);
            }

            // All servers: if RPC request or response contains term T > currentTerm, set currentTerm and convert to follower.
            if (state == RaftState.FOLLOWER) {
                return;
            }

            // Candidate: if votes received from majority of servers, become leader
            System.out.println("election received vote " + receivedVote.intValue());
            // include myself, this is the majority vote
            if (receivedVote.intValue() >= (peers.size() / 2)) {
                state = RaftState.LEADER;
                //TODO: do something as leader
            }

            // Candidate: if election timeout elapses: start new election
            votedFor = NULL_VOTE;
        }
    }

    class HeartBeatTask implements Runnable {
        @Override
        public void run() {
            if (state != RaftState.LEADER) {
                // do some thing as leader
                return;
            }

            long currentTime = System.currentTimeMillis();
//            if (currentTime - lastHeartBeatTime < NodeConfig.HEARTBEAT_INTERVAL_MS) {
//                return;
//            }
        }
        // nested class so that we can use Node's private variable
    }
}
