package raft;

import application.storage.InMemoryStorage;
import application.storage.JsonLogStorage;
import application.storage.LogStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import raft.concurrentutil.SleepHelper;
import raft.logmodule.RaftLogEntry;
import raft.nodemodule.AddressBook;
import raft.nodemodule.Node;
import raft.nodemodule.NodeConfig;
import raft.nodemodule.NodeInfo;
import raft.nodemodule.RaftClientRequest;
import raft.statemachinemodule.RaftCommand;
import raft.statemachinemodule.RaftState;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.io.File;

public class RaftTest {
    final static Logger logger = LogManager.getLogger(RaftTest.class);

    @BeforeClass
    public static void beforeClass() {
        logger.debug("start testing");
        java.util.logging.Logger.getLogger("io.grpc.netty.shaded.io.grpc.netty.NettyClientHandler").setLevel(Level.OFF);
    }

    @Test
    public void playGround() {
        logger.debug(String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    @Test
    public void testLogReplication() throws InterruptedException {
        NodeConfig config = new NodeConfig();

        NodeInfo node1 = new NodeInfo(8258, 8258, "localhost");
        NodeInfo node2 = new NodeInfo(8259, 8259, "localhost");
        NodeInfo node3 = new NodeInfo(8260, 8260, "localhost");
        NodeInfo[] allNodes = new NodeInfo[]{node1, node2, node3};


        // start nodes
        Node[] nodes = {
                new Node(config, new AddressBook(node1, allNodes), new InMemoryStorage()),
                new Node(config, new AddressBook(node2, allNodes), new InMemoryStorage()),
                new Node(config, new AddressBook(node3, allNodes), new InMemoryStorage())
        };
        Thread[] nodeThreads = {new Thread(nodes[0]), new Thread(nodes[1]), new Thread(nodes[2])};
        for(int i = 0; i < nodes.length; i++) {
            nodeThreads[i].start();
        }

        SleepHelper.sleep(10000); // wait for 5 seoncds unitl there is aleader

        for(Node node:nodes) {
            System.out.println("Node: " + node.nodeId + "State: " + node.getState());
            if (node.getState().equals(RaftState.LEADER)) {
                node.handleClientRequest(new RaftClientRequest(
                        RaftCommand.PUT,
                        "abd",
                        "123"
                ));
                node.handleClientRequest(new RaftClientRequest(
                        RaftCommand.PUT,
                        "def",
                        "123"
                ));
                node.handleClientRequest(new RaftClientRequest(
                        RaftCommand.PUT,
                        "gsasd",
                        "123"
                ));
                break;
            }
        }

        SleepHelper.sleep(10000);

        for(int i = 0; i < nodes.length; i++) {
            nodeThreads[i].interrupt();
            nodes[i].destroy();
        }

        SleepHelper.sleep(10000);
        System.err.println("Start prining logs");
        for(Node node:nodes) {
            SleepHelper.sleep(2000);
            System.err.println("Logs for Node: " + node.nodeId);
            for (RaftLogEntry log: node.getLogModule().getAllLogs()) {
                System.out.println(log);
            }
        }
    }

    @Test
    public void testInitialElection() throws InterruptedException {
        NodeConfig config = new NodeConfig();

        NodeInfo node1 = new NodeInfo(8258, 8258, "localhost");
        NodeInfo node2 = new NodeInfo(8259, 8259, "localhost");
        NodeInfo node3 = new NodeInfo(8260, 8260, "localhost");
        NodeInfo[] allNodes = new NodeInfo[]{node1, node2, node3};


        // start nodes
        Node[] nodes = {
                new Node(config, new AddressBook(node1, allNodes), new InMemoryStorage()),
                new Node(config, new AddressBook(node2, allNodes), new InMemoryStorage()),
                new Node(config, new AddressBook(node3, allNodes), new InMemoryStorage())
        };
        Thread[] nodeThreads = {new Thread(nodes[0]), new Thread(nodes[1]), new Thread(nodes[2])};
        for(int i = 0; i < nodes.length; i++) {
            nodeThreads[i].start();
            int finalI = i;
        }

        // run some test
        int leaderId = -1;
        for(int iter = 0; iter < 10; iter++) {
            // wait some time
            long ms = NodeConfig.ELECTION_TIMEOUT_MIN+ ThreadLocalRandom.current().nextLong(NodeConfig.ELECTION_TIMEOUT_RANGE);
            TimeUnit.MILLISECONDS.sleep(ms);
            for(Node node: nodes) {
                if (node.getState().equals(RaftState.LEADER)) {
                    System.err.println("At Iteration: " + iter + " we have one leader: " + node.nodeId);
                    if (leaderId == -1) {
                        leaderId = node.nodeId;
                    } else{
                        Assert.assertEquals(leaderId, node.nodeId);
                    }
                }
            }
        }

        // check a leader exist
        int numLeader = 0;
        for(Node node: nodes) {
            if (node.getState().equals(RaftState.LEADER)) {
                numLeader += 1;
            }
        }
        Assert.assertEquals(1, numLeader);

        // destroy
        for(int i = 0; i < nodes.length; i++) {
            nodeThreads[i].interrupt();
            nodes[i].destroy();
        }
    }

    @Test
    public void testLogSaving() {
        LogStorage storage = new JsonLogStorage();
        storage.setLogName("test");
        RaftLogEntry sample = new RaftLogEntry(0, 0, RaftCommand.GET, "", "");
        RaftLogEntry sample2 = new RaftLogEntry(0, 1, RaftCommand.GET, "", "");
        storage.add(System.currentTimeMillis(), sample, 1);
        storage.add(System.currentTimeMillis(), sample2, 1);
        File jsLog = new File("test.json");
        File historyJsLog = new File("history_test.json");
        boolean exists = jsLog.exists();
        boolean exists2 = historyJsLog.exists();
        Assert.assertEquals(true, exists);
        Assert.assertEquals(true, exists2);
        try {
            jsLog.delete();
            historyJsLog.delete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
