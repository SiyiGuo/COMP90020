package raft.periodictask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.nodemodule.Node;
import raft.nodemodule.NodeInfo;
import raft.statemachinemodule.RaftState;

import java.util.ArrayList;
import java.util.Map;

public class LeaderLogReplicationTask implements Runnable {
    public final static Logger logger = LogManager.getLogger(LeaderLogReplicationTask.class);

    private volatile Node node;
    public LeaderLogReplicationTask(Node nodehook) {
        this.node = nodehook;
    }

    @Override
    public void run() {
        if (this.node.getState() != RaftState.LEADER) {
            return;
        }
        /*
        TODO:
            • If successful: update nextIndex and matchIndex for
            follower (§5.3)
            • If AppendEntries fails because of log inconsistency:
            decrement nextIndex and retry (§5.3)

         */
        for(NodeInfo nodeInfo: this.node.addressBook.getPeerInfo()) {
            if (this.node.getLogModule().getLastIndex() >= this.node.getNextIndex(nodeInfo.nodeId)) {
                /*
                If last log index ≥ nextIndex for a follower: send
                AppendEntries RPC with log entries starting at nextIndex
                 */
            }
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
