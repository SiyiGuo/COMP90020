package raft.nodemodule;

public class NodeAddress {
    public final int port;
    public final String hostname;

    public NodeAddress(int port, String hostname) {
        this.port = port;
        this.hostname = hostname;
    }
}
