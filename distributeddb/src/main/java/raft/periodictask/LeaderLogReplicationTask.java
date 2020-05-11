package raft.periodictask;

import raft.nodemodule.Node;
import raft.statemachinemodule.RaftState;

public class LeaderLogReplicationTask implements Runnable {
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
            If last log index ≥ nextIndex for a follower: send
            AppendEntries RPC with log entries starting at nextIndex
            • If successful: update nextIndex and matchIndex for
            follower (§5.3)
            • If AppendEntries fails because of log inconsistency:
            decrement nextIndex and retry (§5.3)
            • If there exists an N such that N > commitIndex, a majority
            of matchIndex[i] ≥ N, and log[N].term == currentTerm:
            set commitIndex = N (§5.3, §5.4).
         */
    }
}
