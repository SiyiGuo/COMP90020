package raft.nodemodule;


import java.util.ArrayList;

public class NodeConfig {
    public static class NodeAddress {
        public final int port;
        public final String hostname;

        public NodeAddress(int port, String hostname) {
            this.port = port;
            this.hostname = hostname;
        }
    }

    public final int listenPort;
    public final ArrayList<NodeAddress> peers;

    // RequestVote config
    public static final long HEARTBEAT_INTERVAL_MS = (long)5 * 1000;
    public static final long ELECTION_INTERVAL_MS = (long)15 * 1000;

    /* these are related together */
    public static final int ELECTION_TIMEOUT_MIN = 1500;
    public static final int ELECTION_TIMEOUT_RANGE = 2000;
    public static final int TASK_DELAY = 500;
    public static final int RPC_RESULT_WAIT_TIME = 1000;

    public NodeConfig(int listenPort, String[] peers) {
        this.listenPort = listenPort;
        this.peers = new ArrayList<>();


        for(String peer: peers) {
            String[] parts = peer.split(":");
            this.peers.add(new NodeAddress(Integer.parseInt(parts[1]), parts[0]));
        }
    }
}
