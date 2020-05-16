package raft.nodemodule;

import java.util.ArrayList;

public class AddressBook {
    // peers, exlucde this node it self
    private ArrayList<NodeInfo> peers;
    // info about self
    private NodeInfo self;

    /*
    TODO: have a leader ID.
    Setup when ever leader info is updated
     */
    private volatile int leaderId;

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

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    // Determine whether we reached majority vote
    public boolean isMajorityVote(int number) {
        // 1+ 5 peer. ceil(5/2) = 3 + 1 = majority of 5 nodes
        // 1+6peer. ceil(6/2)) = 3+1 = majority of 7 nodes
        return number >= Math.ceil(this.peers.size() / 2);
    }
}
