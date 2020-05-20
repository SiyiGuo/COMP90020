package raft.periodictask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.concurrentutil.Cu;
import raft.concurrentutil.RaftStaticThreadPool;
import raft.consensusmodule.RaftAppendEntriesArgs;
import raft.consensusmodule.RaftAppendEntriesResult;
import raft.logmodule.RaftLogEntry;
import raft.nodemodule.Node;
import raft.nodemodule.NodeInfo;
import raft.ruleset.RulesForServers;
import raft.statemachinemodule.RaftState;

import java.util.ArrayList;
import java.util.List;

public class LeaderLogReplicationTask implements Runnable {
    public final static Logger logger = LogManager.getLogger(LeaderLogReplicationTask.class);

    private volatile Node node;
    public LeaderLogReplicationTask(Node nodehook) {
        this.node = nodehook;
    }

    public void replicateLogForNode(NodeInfo nodeInfo) {
         /*
        If last log index ≥ nextIndex for a follower: send
        AppendEntries RPC with log entries starting at nextIndex
         */
        long lastIndex = this.node.getLogModule().getLastIndex();
        long nodeNextIndex = this.node.getNodeNextIndex(nodeInfo.nodeId);
        logger.debug("Current Node{} Node{} lastIndex{} nodeNextIndex{} nodeMatchIndex{}",
                this.node.nodeId, nodeInfo.nodeId, lastIndex, nodeNextIndex,
                this.node.getNodeMatchIndex(nodeInfo.nodeId)
        );
        if (lastIndex >= nodeNextIndex) {
            // prepare the entry
            RaftStaticThreadPool.execute(() -> {
                if (!(this.node.getState() == RaftState.LEADER)) {
                    return;
                }

                long prevLogIndex = this.node.getNodeNextIndex(nodeInfo.nodeId) - 1;
                long pevLogTerm;
                if (prevLogIndex == 0) {
                    pevLogTerm = this.node.getCurrentTerm();
                } else{
                    pevLogTerm = this.node.getLogModule().getLog(prevLogIndex).term;
                }

                List<RaftLogEntry> entries = this.node.getLogModule().getLogsOnStartIndex(nodeNextIndex);
                RaftAppendEntriesArgs request = new RaftAppendEntriesArgs(
                        this.node.getCurrentTerm(),
                        this.node.nodeId,
                        prevLogIndex,
                        pevLogTerm,
                        entries,
                        this.node.getCommitIndex()
                );

                RaftAppendEntriesResult result;
                try {
                     result = this.node.getNodeRpcClient(nodeInfo.nodeId).appendEntries(request);
                } catch (Exception e) {
                    Cu.debug("Errpr when appendEntries for: " + nodeInfo.nodeId);
                    Cu.debug("Args:");
                    Cu.debug(request);
                    e.printStackTrace();
                    return;
                }

                Cu.debug("Node: "+nodeInfo.nodeId + "AppendEntries Response Back: \n" + result);

                if (RulesForServers.compareTermAndBecomeFollower(result.term, this.node)) {
                    return;
                }

                if (result.success) {
                    // • If successful: update nextIndex and matchIndex for follower (§5.3)
                    long newMatchIndex = this.node.getLogModule().getLastIndex();
                    long newNextIndex = newMatchIndex + entries.size();
                    this.node.updateMatchIndex(nodeInfo.nodeId, newMatchIndex);
                    this.node.updateNodeNextIndex(nodeInfo.nodeId, newNextIndex);
                } else {
                    // If AppendEntries fails because of log inconsistency:
                    // decrement nextIndex and retry (§5.3)
                    long newNextIndex = nodeNextIndex - 1;
                    this.node.updateNodeNextIndex(nodeInfo.nodeId, newNextIndex);
                    // and retry
                    this.replicateLogForNode(nodeInfo);
                }
                return;
            }, false);
        }
    }

    @Override
    public void run() {
        if (this.node.getState() != RaftState.LEADER) {
            return;
        }
        for(NodeInfo nodeInfo: this.node.addressBook.getPeerInfo()) {
            // this might be called recursively
            this.replicateLogForNode(nodeInfo);
        }

        /*
        • If there exists an N such that N > commitIndex, a majority
          of matchIndex[i] ≥ N,  and log[N].term == currentTerm:
          set commitIndex = N (§5.3, §5.4).
         */
        // Get all N > commit index.
        ArrayList<Long> allIndexCandidates = new ArrayList<>();
        for (long matchIndex: this.node.getAllMatchIntex()) {
            // If there exists an N such that N > commitIndex
            System.out.println(String.format("m:%s c:%s", matchIndex, this.node.getCommitIndex()));
            if (matchIndex > this.node.getCommitIndex()) {
                allIndexCandidates.add(matchIndex);
            }
        }
        allIndexCandidates.sort(Long::compareTo); // sort all N > commit index from small to big
        allIndexCandidates.forEach(num->System.out.println("***: " + num));

        int i = 0;
        for(long N: allIndexCandidates) {
            // a majority of matchIndex[i] ≥ N。 Since Arraylist is sorted, all matchIndex[i:] >= N;
            System.out.println(String.format("getLogModule:%s currentTerm:%s",
                    this.node.getLogModule().getLog(N),
                    this.node.getCurrentTerm()));
            if (this.node.addressBook.isMajorityVote(allIndexCandidates.size() - i)) {
                // and log[N].term == currentTerm:
                if (this.node.getLogModule().getLog(N).term == this.node.getCurrentTerm()) {
                    // set commitIndex = N (§5.3, §5.4).
                    this.node.setCommitIndex(N);
                    logger.debug("Majority vote achieved, set commit index to: " + N);
                    break;
                }
            }
            i += 1;
        }
    }
}
