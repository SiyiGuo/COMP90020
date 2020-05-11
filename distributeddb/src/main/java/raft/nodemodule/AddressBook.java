package raft.nodemodule;

import java.util.ArrayList;

public class AddressBook {
    // peers, exlucde this node it self
    private ArrayList<NodeInfo> peers;
    // info about self
    private NodeInfo self;
    // leader across all nodes
    //private int leaderId;

    public AddressBook(NodeInfo self, NodeInfo[] allNodes) {
        this.self = self;
        this.peers = new ArrayList<>();
        for (NodeInfo node : allNodes) {
            if (node != this.self) {
                this.peers.add(node);
            }
        }
    }

    public NodeInfo getSelfInfo() {
        return self;
    }

    /*
    Return info about all peers except current node
     */
    public ArrayList<NodeInfo> getPeerInfo() {
        return peers;
    }

    public void addPeer(NodeInfo newPeer) {
        this.peers.add(newPeer);
    }
}
