package raft.rpcmodule;

import org.junit.Assert;
import org.junit.Test;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.logmodule.RaftLogEntry;

import java.io.IOException;
import java.util.ArrayList;

public class RequestVoteRpcTest {
    @Test
    public void testCommunication() {
        Thread servcerThread = new Thread(() -> {
            RaftRpcServer server = new RaftRpcServer(8237, null);
            try {
                System.out.println("Start server");
                server.start();
                server.blockUntilShutdown();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        });
        servcerThread.start();

        RaftRpcClient client = new RaftRpcClient("localhost", 8237);
        ArrayList<RaftRequestVoteResult> responses = new ArrayList<>();
        int numRequest = 10;
        for (int i = 0; i < numRequest; i++) {
            RaftRequestVoteArgs args = new RaftRequestVoteArgs(
                    i,0, -i, i
            );
            responses.add(client.requestVote(args));
        }
        Assert.assertEquals(numRequest, responses.size());
    }
}
