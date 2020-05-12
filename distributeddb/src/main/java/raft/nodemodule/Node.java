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
import raft.periodictask.ElectionTask;
import raft.periodictask.HeartBeatTask;
import raft.periodictask.LeaderLogReplicationTask;
import raft.rpcmodule.RaftRpcClient;
import raft.rpcmodule.RaftRpcServer;
import raft.statemachinemodule.RaftState;
import raft.statemachinemodule.RaftStateMachine;
import raft.ruleSet.RulesForServers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
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
    public HashMap<Integer, RaftRpcClient> peers;


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
    private volatile ConcurrentHashMap<Integer, Long> nextIndex;
    private volatile ConcurrentHashMap<Integer, Long> matchIndex;

    // time variable
    private volatile long lastHeartBeatTime = 0;
    private volatile long lastElectionTime = 0;
    private volatile long timeOut = 0;
    // Task
    private HeartBeatTask heartBeatTask;
    private ElectionTask electionTask;
    private LeaderLogReplicationTask replicationTask;
    public RaftThreadPool threadPool;
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

        this.heartBeatTask = new HeartBeatTask(this);
        this.electionTask = new ElectionTask(this);
        this.replicationTask = new LeaderLogReplicationTask(this);
        // TODO: replicationTask
        // this.replicationTask = new ReplicationTask()
    }

    @Override
    public void init() {
        if (started) return;

        /*
        Run the initilization of the server
         */
        // create create Peer Client
        this.peers = new HashMap<>();
        for (NodeInfo peer : this.addressBook.getPeerInfo()) {
            this.peers.put(peer.nodeId, new RaftRpcClient(peer.hostname, peer.listenPort));
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
        this.threadPool.scheduleWithFixedDelay(replicationTask, NodeConfig.TASK_DELAY);

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
        this.nextIndex = new ConcurrentHashMap<>();
        this.matchIndex = new ConcurrentHashMap<>();
        long initIndex = this.logModule.getLastIndex() + 1;
        for(NodeInfo info: addressBook.getPeerInfo()) {
            this.nextIndex.put(info.nodeId, initIndex);
            this.matchIndex.put(info.nodeId, 0L);
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
        for (RaftRpcClient peer : peers.values()) {
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
    Out own getter and setter
     */
    public long getNodeNextIndex(int nodeId) {
        return this.nextIndex.get(nodeId);
    }

    public void updateNodeNextIndex(int nodeId, long newNextIndex) {
        this.nextIndex.put(nodeId, newNextIndex);
    }

    public void updateMatchIndex(int nodeId, long newMatchIndex) {
        this.matchIndex.put(nodeId, newMatchIndex);
    }

    public RaftRpcClient getNodeRpcClient(int nodeId) {
        return this.peers.get(nodeId);
    }

    public Collection<RaftRpcClient> getAllPeerRpfClient() {
        return this.peers.values();
    }

    public Collection<Long> getAllMatchIntex() {
        return this.matchIndex.values();
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

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }


}
