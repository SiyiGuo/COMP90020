package raft.nodemodule;

/*
Collection of node info.
Should not
 */
public class NodeInfo {
    public final int nodeId;
    public final NodeAddress nodeAddress;

    public NodeInfo(int nodeId, NodeAddress nodeAddress) {
        this.nodeId = nodeId;
        this.nodeAddress = nodeAddress;
    }
}
