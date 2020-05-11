package raft.nodemodule;

/*
Collection of node info.
Should not
 */
public class NodeInfo {
    public final int nodeId;
    public final int listenPort;
    public final String hostname;
    
    public NodeInfo(int nodeId, int listenPort, String hostname) {
        this.nodeId = nodeId;
        this.listenPort = listenPort;
        this.hostname = hostname;
    }
}
