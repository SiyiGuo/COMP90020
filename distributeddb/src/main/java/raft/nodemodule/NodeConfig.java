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

    public NodeConfig(int listenPort, String[] peers) {
        this.listenPort = listenPort;
        this.peers = new ArrayList<>();

        for(String peer: peers) {
            String[] parts = peer.split(":");
            this.peers.add(new NodeAddress(Integer.parseInt(parts[1]), parts[0]));
        }
    }
}
