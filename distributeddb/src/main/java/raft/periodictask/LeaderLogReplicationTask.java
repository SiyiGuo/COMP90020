package raft.periodictask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.consensusmodule.RaftAppendEntriesArgs;
import raft.consensusmodule.RaftAppendEntriesResult;
import raft.nodemodule.Node;
import raft.nodemodule.NodeInfo;
import raft.ruleset.RulesForServers;
import raft.statemachinemodule.RaftState;

import java.util.ArrayList;

public class LeaderLogReplicationTask implements Runnable {
    public final static Logger logger = LogManager.getLogger(LeaderLogReplicationTask.class);

    private volatile Node node;
    public LeaderLogReplicationTask(Node nodehook) {
        this.node = nodehook;
    }

    public void replicateLog(NodeInfo nodeInfo) {
         /*
        If last log index ≥ nextIndex for a follower: send
        AppendEntries RPC with log entries starting at nextIndex
         */
        long lastIndex = this.node.getLogModule().getLastIndex();
        long nodeNextIndex = this.node.getNodeNextIndex(nodeInfo.nodeId);
        if (lastIndex >= nodeNextIndex) {
            logger.debug("Node{} lastIndex{} nodeNextIndex{}", nodeInfo.nodeId, lastIndex, nodeNextIndex);
            // prepare the entry
            this.node.threadPool.execute(() -> {
                if (!(this.node.getState() == RaftState.LEADER)) {
                    return;
                }

                RaftAppendEntriesArgs request = new RaftAppendEntriesArgs(
                        this.node.getCurrentTerm(),
                        this.node.nodeId,
                        lastIndex,
                        this.node.getLogModule().getLast().term,
                        this.node.getLogModule().getLogsOnStartIndex(nodeNextIndex),
                        this.node.getCommitIndex()
                );
                RaftAppendEntriesResult result = this.node.getNodeRpcClient(nodeInfo.nodeId).appendEntries(request);
                logger.debug("Node{} lastIndex{} nodeNextIndex{} AppendEntries Response {}"
                        , nodeInfo.nodeId, lastIndex, nodeNextIndex, result);

                if (RulesForServers.compareTermAndBecomeFollower(result.term, this.node)) {
                    return;
                }

                if (result.success) {
                    // • If successful: update nextIndex and matchIndex for follower (§5.3)
                    long newMatchIndex = this.node.getLogModule().getLastIndex();
                    long newNextIndex = newMatchIndex + 1;
                    this.node.updateMatchIndex(nodeInfo.nodeId, newMatchIndex);
                    this.node.updateNodeNextIndex(nodeInfo.nodeId, newNextIndex);
                } else {
                    // If AppendEntries fails because of log inconsistency:
                    // decrement nextIndex and retry (§5.3)
                    long newNextIndex = nodeNextIndex - 1;
                    this.node.updateNodeNextIndex(nodeInfo.nodeId, newNextIndex);
                    // and retry
                    this.replicateLog(nodeInfo);
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
            this.replicateLog(nodeInfo);
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
            if (matchIndex > this.node.getCommitIndex()) {
                allIndexCandidates.add(matchIndex);
            }
        }
        allIndexCandidates.sort(Long::compareTo); // sort all N > commit index from small to big

        int i = 0;
        for(long N: allIndexCandidates) {
            i += 1;
            // a majority of matchIndex[i] ≥ N。 Since Arraylist is sorted, all matchIndex[i:] >= N;
            if (this.node.addressBook.isMajorityVote(allIndexCandidates.size() - i)) {
                // and log[N].term == currentTerm:
                if (this.node.getLogModule().getLog(N).term == this.node.getCurrentTerm()) {
                    // set commitIndex = N (§5.3, §5.4).
                    this.node.setCommitIndex(N);
                    logger.debug("Majority vote achieved, set commit index to: " + N);
                    break;
                }
            }
        }
    }
}
