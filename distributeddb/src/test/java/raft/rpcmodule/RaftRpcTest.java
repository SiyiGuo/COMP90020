package raft.rpcmodule;

import org.junit.Assert;
import org.junit.Test;
import raft.consensusmodule.RaftAppendEntriesArgs;
import raft.consensusmodule.RaftAppendEntriesResult;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.logmodule.RaftLogEntry;
import raft.nodemodule.Node;
import raft.nodemodule.NodeConfig;

import java.io.IOException;
import java.util.ArrayList;

public class RaftRpcTest {

    @Test
    public void RequestVoteRpcTest() {
        NodeConfig config = new NodeConfig(
               8213,
                new String[]{}
        );
        Node node = new Node(config);
        Thread serverThread = new Thread(node);
        serverThread.start();

        RaftRpcClient client = new RaftRpcClient("localhost", 8213);
        ArrayList<RaftRequestVoteResult> responses = new ArrayList<>();
        int numRequest = 10;
        for (int i = 0; i < numRequest; i++) {
            RaftRequestVoteArgs args = new RaftRequestVoteArgs(
                    i,0, -i, i
            );
            responses.add(client.requestVote(args));
        }
        Assert.assertEquals(numRequest, responses.size());

        serverThread.interrupt();
        node.destroy();
    }

    public void AppendEntriesRpcTest() {
        NodeConfig config = new NodeConfig(
                8213,
                new String[]{}
        );
        Node node = new Node(config);
        Thread serverThread = new Thread(node);
        serverThread.start();

        RaftRpcClient client = new RaftRpcClient("localhost", 8213);
        ArrayList<RaftAppendEntriesResult> responses = new ArrayList<>();
        int numRequest = 10;
        for (int i = 0; i < numRequest; i++) {
            RaftAppendEntriesArgs args = new RaftAppendEntriesArgs(
                    i, 0, i, i, new ArrayList<>(), i
            );
            responses.add(client.appendEntries(args));
        }
        Assert.assertEquals(numRequest, responses.size());

        serverThread.interrupt();
        node.destroy();
    }
}
