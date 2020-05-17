package raft.periodictask;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.concurrentutil.RaftStaticThreadPool;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.nodemodule.Node;
import raft.nodemodule.NodeConfig;
import raft.rpcmodule.RaftRpcClient;
import raft.ruleset.RulesForServers;
import raft.statemachinemodule.RaftState;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ElectionTask implements Runnable {
    public final static Logger logger = LogManager.getLogger(ElectionTask.class);

    private volatile Node node;
    public ElectionTask(Node nodehook) {
        this.node = nodehook;
    }

    @Override
    public void run() {
        // look into time stamp
        if (this.node.getState() == RaftState.LEADER) return;

            /*
            Work as Follower / Candidate
            Followers (§5.2): If election timeout elapses without receiving AppendEntries
            RPC from current leader or granting vote to candidate:
            convert to candidate
            Candidates (§5.2): If election timeout elapses: start new election
             */
        // wait for a random amount of variable
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.node.getLastElectionTime() < this.node.getTimeOut()) return;

            /*
           start election.
            */
        this.node.setLastElectionTime(System.currentTimeMillis());
        this.node.setRandomTimeout();

        this.node.setCurrentTerm(this.node.getCurrentTerm() + 1);  // increments its current term
        this.node.setState(RaftState.CANDIDATE); // transition sot candidate state
        this.node.setVotedFor(this.node.nodeId);// vote for itself

        //RequestVoteRPC in parallel to other peers
        ArrayList<Future> results = new ArrayList<>();
        for (RaftRpcClient peer : this.node.getAllPeerRpfClient()) {
            results.add(RaftStaticThreadPool.submit(() -> {
                try {
                    RaftRequestVoteArgs request = new RaftRequestVoteArgs(
                            this.node.getCurrentTerm(),
                            this.node.nodeId,
                            this.node.getLogModule().getLastIndex(),
                            this.node.getLogModule().getLast().term
                    );
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
            RaftStaticThreadPool.submit(() -> {
                try {
                    if (this.node.getState() == RaftState.FOLLOWER) return -1;
                    RaftRequestVoteResult result = (RaftRequestVoteResult) peerResult.get(NodeConfig.RPC_RESULT_WAIT_TIME, MILLISECONDS);
                    if (result == null) {
                        return -1;
                    }
                    logger.info("election task received result: term {} voteGranted {}", result.term, result.voteGranted);

                    // Response handling
                    if (RulesForServers.compareTermAndBecomeFollower(result.term, this.node)) {
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
            logger.warn("Node {} election task interrupted", this.node.nodeId);
        }

        // All servers:
        // if RPC request or response contains term T > currentTerm, set currentTerm and convert to follower.
        if (this.node.getState() == RaftState.FOLLOWER) {
            return;
        }

        // Candidate: if votes received from majority of servers, become leader
        System.out.println("election received vote " + receivedVote.intValue());
        // include myself, this is the majority vote
        if (this.node.addressBook.isMajorityVote(receivedVote.intValue())) {
            this.node.setState(RaftState.LEADER);
            this.node.addressBook.setLeaderId(this.node.nodeId);
            this.node.actionsWhenBecameLeader();
        }

        // Candidate: if election timeout elapses: start new election
        this.node.setVotedFor(Node.NULL_VOTE);
    }
}
