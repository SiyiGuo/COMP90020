package raft.rpcmodule;

import org.junit.Test;

import java.io.IOException;

public class RequestVoteRpcTest {
    @Test
    public void testCommunication() {
        Thread servcerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestVoteServer server = new RequestVoteServer(8237);
                try {
                    System.out.println("Start server");
                    server.start();
                    server.blockUntilShutdown();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        servcerThread.start();

        RequestVoteClient client = new RequestVoteClient("localhost", 8237);
        for (int i = 0; i < 10; i++) {
            client.greet("Test: "+i);
        }
    }
}
