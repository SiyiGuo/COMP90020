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
import raft.logmodule.RaftLogEntry;
import raft.logmodule.RaftLogModule;
import raft.rpcmodule.RaftRpcClient;
import raft.rpcmodule.RaftRpcServer;
import raft.statemachinemodule.RaftState;
import raft.statemachinemodule.RaftStateMachine;
import raft.ruleSet.RulesForServers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
Node keep state of the class
 */
public class Node implements LifeCycle, Runnable {
    public final static Logger logger = LogManager.getLogger(Node.class);
    public final static int NULL_VOTE = -1;
    public final int nodeId;
    public final Node nodehook;

    /* Engineering Variables*/
    // config for this node
    public final NodeConfig config;

    /* Peers
    Peer may be down but never deleted
    TODO: When add new PEER
    add both RPC client, and adressBook
     */
    public final AddressBook addressBook;
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
    private volatile HashMap<Integer, Long> nextIndex;
    private volatile HashMap<Integer, Long> matchIndex;

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

    public Node(NodeConfig config, AddressBook addressBook) {
        this.addressBook = addressBook;
        this.nodeId = this.addressBook.getSelfInfo().nodeId;
        this.nodehook = this;

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
        // create create Peer Client
        this.peers = new ArrayList<>();
        for (NodeInfo peer : this.addressBook.getPeerInfo()) {
            this.peers.add(new RaftRpcClient(peer.hostname, peer.listenPort));
        }

        // create thread pool
        this.threadPool = new RaftThreadPool(Integer.toString(this.nodeId));
    }

    public void startNodeRunning() {
        /*
        Actual initial sequence
        Actual initial sequence
         */
        // run rpc server
        this.rpcServer = new RaftRpcServer(this.addressBook.getSelfInfo().listenPort, this);
        this.threadPool.execute(rpcServer);
        this.threadPool.scheduleWithFixedDelay(heartBeatTask, NodeConfig.TASK_DELAY);
        this.threadPool.scheduleAtFixedRate(electionTask, 6000, NodeConfig.TASK_DELAY);

        // start 3 module
        this.logModule = new RaftLogModule();
        this.consensus = new RaftConsensus(this);
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
        return this.consensus.handleRequestVote(args);
    }

    public RaftAppendEntriesResult handleAppendEntries(RaftAppendEntriesArgs args) {
        return this.consensus.handleAppendEntries(args);
    }

    public ClientResponse handleClientRequest(ClientRequest req) {
        /*
        TODO:
        If command received from client.

        Append entry to local log

        respond after entry applied to state machine
         */
        return null;
    }

    // redirect to leader
    public ClientResponse redirect(ClientRequest req) {
        int leaderId = addressBook.getLeaderId();
        // TODO: somehow call the leader
        // reutrn this.leader.handleCLientRequest(req)
        return null;
    }

    public void actionsWhenBecameLeader() {
        /*
        Leaders:
        Upon election: send initial empty AppendEntries RPCs (heartbeat) to each server; repeat during idle periods to
        prevent election timeouts (§5.2)
        */
        sendEmptyAppendEntries();

        /*
        Volatile state on leaders:
        (Reinitialized after election)
        nextIndex[] for each server, index of the next log entry bto send to that server
                    (initialized to leader last log index + 1)
        matchIndex[] for each server, index of highest log entry known to be replicated on server
                    (initialized to 0, increases monotonically)
         */
        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();
        long initIndex = this.logModule.getLastIndex() + 1;
        for(NodeInfo info: addressBook.getPeerInfo()) {
            this.nextIndex.put(info.nodeId, initIndex);
            this.matchIndex.put(info.nodeId, (long) 0);
        }

    }

    public void sendEmptyAppendEntries() {
        if (this.state != RaftState.LEADER) {
            logger.error("Node {} is not leader but triggered leader task", this.nodeId);
            return;
        }

        /*
        Start Append empty entries
         */
        for (RaftRpcClient peer : peers) {
            threadPool.execute(() -> {
                try {
                    if(this.state != RaftState.LEADER) {
                        return;
                    }

                    RaftAppendEntriesArgs request = new RaftAppendEntriesArgs(
                            currentTerm,
                            nodeId,
                            logModule.getLastIndex(),
                            logModule.getLast().term,
                            new ArrayList<RaftLogEntry>(),
                            commitIndex
                    );
                    RaftAppendEntriesResult result = peer.appendEntries(request);
                    RulesForServers.compareTermAndBecomeFollower(request.term, nodehook);
                } catch (Exception e) {
                    logger.error("HeadBeat Task RPF fail.");
                }
            }, false);
        }
    }

    class ElectionTask implements Runnable {
        // nested class such that can use Node's private variable
        @Override
        public void run() {
            // look into time stamp
            if (state == RaftState.LEADER) return;

            /*
            Work as Follower / Candidate
            Followers (§5.2): If election timeout elapses without receiving AppendEntries
            RPC from current leader or granting vote to candidate:
            convert to candidate
            Candidates (§5.2): If election timeout elapses: start new election
             */
            // wait for a random amount of variable
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastElectionTime < timeOut) return;

            /*
           start election.
            */
            lastElectionTime = System.currentTimeMillis();
            setRandomTimeout();

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
                        logger.error(e);
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
                        if (state == RaftState.FOLLOWER) return -1;
                        RaftRequestVoteResult result = (RaftRequestVoteResult) peerResult.get(NodeConfig.RPC_RESULT_WAIT_TIME, MILLISECONDS);
                        if (result == null) {
                            return -1;
                        }
                        logger.info("election task received result: term {} voteGranted {}", result.term, result.voteGranted);

                        if (RulesForServers.compareTermAndBecomeFollower(result.term, nodehook)) {
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

            // All servers:
            // if RPC request or response contains term T > currentTerm, set currentTerm and convert to follower.
            if (state == RaftState.FOLLOWER) {
                return;
            }

            // Candidate: if votes received from majority of servers, become leader
            System.out.println("election received vote " + receivedVote.intValue());
            // include myself, this is the majority vote
            if (receivedVote.intValue() >= (peers.size() / 2)) {
                state = RaftState.LEADER;

                actionsWhenBecameLeader();
            }

            // Candidate: if election timeout elapses: start new election
            votedFor = NULL_VOTE;
        }
    }

    /*
    Invoked by leader to replicate log entries. Also used as heartbeat
     */
    class HeartBeatTask implements Runnable {
        @Override
        public void run() {
            if (state != RaftState.LEADER) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastHeartBeatTime < NodeConfig.HEARTBEAT_INTERVAL_MS) {
                return;
            }
            lastHeartBeatTime = System.currentTimeMillis();

            // Send Out Heartbeat
            sendEmptyAppendEntries();
        }
    }

    public void setRandomTimeout() {
        this.timeOut = ThreadLocalRandom.current().nextLong(NodeConfig.ELECTION_TIMEOUT_RANGE) + NodeConfig.ELECTION_TIMEOUT_MIN;
    }

    @Override
    public String toString() {
        return String.format(
                "NodeId: %s, currentTerm: %s, votedFor:%s", this.nodeId, this.currentTerm, this.votedFor
        );
    }

    /*
        Jungle of Getter and Setter
         */
    public long getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public void setLastHeartBeatTime(long lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    public long getLastElectionTime() {
        return lastElectionTime;
    }

    public void setLastElectionTime(long lastElectionTime) {
        this.lastElectionTime = lastElectionTime;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public RaftLogModule getLogModule() {
        return logModule;
    }

    public void setLogModule(RaftLogModule logModule) {
        this.logModule = logModule;
    }

    public void setState(RaftState state) {
        this.state = state;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(long currentTerm) {
        this.currentTerm = currentTerm;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(int votedFor) {
        this.votedFor = votedFor;
    }
}
