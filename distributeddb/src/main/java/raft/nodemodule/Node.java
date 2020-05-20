package raft.nodemodule;

import application.ServerHandler;
import application.storage.DummyLogStorage;
import application.storage.LogStorage;
import application.storage.Storage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.LifeCycle;
import raft.concurrentutil.RaftStaticThreadPool;
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
import raft.ruleset.RulesForServers;
import raft.statemachinemodule.RaftState;
import raft.statemachinemodule.RaftStateMachine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/*
Node keep state of the class
 */
public class Node implements LifeCycle, Runnable {
    public final static Logger logger = LogManager.getLogger(Node.class);
    public final static int NULL_VOTE = -1;
    public final int nodeId;
    public final Node nodehook;
    public int rpcCount;
    private Storage storage;
    private LogStorage logStorage;

    /* Engineering Variables*/
    // config for this node
    public final NodeConfig config;

    /*
    Peers
    RPC related
     */
    private RaftRpcServer rpcServer;
    public final AddressBook addressBook;
    public HashMap<Integer, RaftRpcClient> peers;

    /*
    ALgorithm related
     */
    private RaftConsensus consensus;
    private volatile RaftLogModule logModule;
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

    private ServerHandler serverHandler;

    public Node(NodeConfig config, AddressBook addressBook, Storage storage, LogStorage logStorage) {
        this.config = config;
        this.rpcCount = 0;

        this.addressBook = addressBook;
        this.storage = storage;
        this.logStorage = logStorage;
        this.nodeId = this.addressBook.getSelfInfo().nodeId;
        this.nodehook = this;

        this.commitIndex = 0;
        this.lastApplied = 0;
        this.state = RaftState.FOLLOWER;

        this.heartBeatTask = new HeartBeatTask(this);
        this.electionTask = new ElectionTask(this);
        this.replicationTask = new LeaderLogReplicationTask(this);
        
        this.serverHandler = new ServerHandler(this);
    }

    public Node(NodeConfig config, AddressBook addressBook, Storage storage) {
        this(config, addressBook, storage, new DummyLogStorage());
    }

    @Override
    public void init() {
        /*
        Run the initilization of the server
         */

        // create create Peer Client
        this.peers = new HashMap<>();
        for (NodeInfo peer : this.addressBook.getPeerInfo()) {
            this.peers.put(peer.nodeId, new RaftRpcClient(peer.hostname, peer.listenPort));
        }
    }

    public void startNodeRunning() {
        /*
        Actual initial sequence
         */

        // run rpc server
        this.rpcServer = new RaftRpcServer(this.addressBook.getSelfInfo().listenPort, this);

        // schedule periodic task
        RaftStaticThreadPool.execute(rpcServer);
        RaftStaticThreadPool.scheduleWithFixedDelay(heartBeatTask, NodeConfig.TASK_DELAY);
        RaftStaticThreadPool.scheduleAtFixedRate(electionTask, 6000, NodeConfig.TASK_DELAY);
        RaftStaticThreadPool.scheduleWithFixedDelay(replicationTask, NodeConfig.TASK_DELAY);

        // start 3 module
        this.logModule = new RaftLogModule(this.logStorage);
        this.consensus = new RaftConsensus(this);
        this.stateMachine = new RaftStateMachine(this.storage);
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

    public RaftClientResponse handleClientRequest(RaftClientRequest req) {
        return this.serverHandler.handleClientRequest(req);
    }

    // redirect to leader
    public RaftClientResponse redirect(RaftClientRequest req) {
        return this.serverHandler.redirect(req);
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
            RaftStaticThreadPool.execute(() -> {
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
                    RulesForServers.compareTermAndBecomeFollower(result.term, nodehook);
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

    public long getNodeMatchIndex(int nodeId) {
        return this.matchIndex.get(nodeId);
    }

    public void updateNodeNextIndex(int nodeId, long newNextIndex) {
        if (newNextIndex < 0) {
            System.err.println("Error with node: " + nodeId);
            System.exit(1);
        }
        this.nextIndex.put(nodeId, newNextIndex);
    }

    public void updateMatchIndex(int nodeId, long newMatchIndex) {
        this.matchIndex.put(nodeId, newMatchIndex);
    }

    public RaftRpcClient getNodeRpcClient(int nodeId) {
        return this.peers.get(nodeId);
    }

    public Collection<RaftRpcClient> getAllPeerRpfClient() { return this.peers.values(); }

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
        // Rules for Servers: All Servers
        // If commitIndex > lastApplied.
        // Increment lastApplied/ Apply log[lastApplied] to state machine.
        System.err.println("-----------setCommitIndex---------------");
        System.err.println(commitIndex);
        System.err.println(this.lastApplied);
        if (this.commitIndex > this.lastApplied) {
            for(long i = this.lastApplied+1; i <= commitIndex; i++) {
                System.err.println(this.logModule.getLog(i) + "applied");
                this.stateMachine.apply(this.logModule.getLog(i));
            }
            this.lastApplied = commitIndex;
        }
    }

    public RaftLogModule getLogModule() {
        return logModule;
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

    public long getTimeOut() { return timeOut; }

    public RaftConsensus getConsensus() {
        return consensus;
    }

    public RaftStateMachine getStateMachine() {
        return stateMachine;
    }

    public Storage getStorage() {
        return storage;
    }

    public long getLastApplied() {
        return lastApplied;
    }

    public ConcurrentHashMap<Integer, Long> getNextIndex() {
        return nextIndex;
    }

    public ConcurrentHashMap<Integer, Long> getMatchIndex() {
        return matchIndex;
    }
}
