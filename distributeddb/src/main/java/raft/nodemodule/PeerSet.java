package raft.nodemodule;

import java.util.ArrayList;

public class PeerSet {
    private ArrayList<NodeInfo> peers;
    private NodeInfo self;
    private int leaderId;

    public PeerSet() {
        ;
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

    public void addPeer() {

    }

    public void setLeader(int newLeaderId) {
        this.leaderId = leaderId;
    }
}
