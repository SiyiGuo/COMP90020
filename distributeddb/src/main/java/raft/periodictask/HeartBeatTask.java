package raft.periodictask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.nodemodule.Node;
import raft.nodemodule.NodeConfig;
import raft.statemachinemodule.RaftState;

/*
    Invoked by leader to replicate log entries. Also used as heartbeat
     */
public class HeartBeatTask implements Runnable {
    public final static Logger logger = LogManager.getLogger(ElectionTask.class);

    private volatile Node node;
    public HeartBeatTask(Node nodehook) {
        this.node = nodehook;
    }

    @Override
    public void run() {
        /*
        Leader send AppendEntries RPC with empty entries as heartbeat
        */
        if (this.node.getState() != RaftState.LEADER) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - this.node.getLastHeartBeatTime() < NodeConfig.HEARTBEAT_INTERVAL_MS) {
            return;
        }
        this.node.setLastHeartBeatTime(System.currentTimeMillis());

        // Send Out Heartbeat
        this.node.sendEmptyAppendEntries();
    }
}
