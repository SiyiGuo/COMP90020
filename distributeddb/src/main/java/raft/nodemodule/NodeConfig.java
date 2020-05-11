package raft.nodemodule;


import java.util.ArrayList;

public class NodeConfig {
    /*
    Static file that should not be modified once created
     */

    public static final long HEARTBEAT_INTERVAL_MS = 1000;
//    public static final long ELECTION_INTERVAL_MS = (long) 15 * 1000;
    /* these are related together */
    public static final int ELECTION_TIMEOUT_MIN = 1500;
    public static final int ELECTION_TIMEOUT_RANGE = 2000;
    public static final int TASK_DELAY = 500;
    public static final int RPC_RESULT_WAIT_TIME = 1000;
}
