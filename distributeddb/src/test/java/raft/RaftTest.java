package raft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.logmodule.LogEntry;
import raft.nodemodule.Node;
import raft.nodemodule.NodeConfig;
import raft.rpcmodule.RequestVoteClient;
import raft.statemachinemodule.RaftState;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RaftTest {
    final static Logger logger = LogManager.getLogger(RaftTest.class);

    @BeforeClass
    public static void beforeClass() {
        logger.debug("start testing");
    }

    @Test
    public void playGround() {
        logger.debug(String.valueOf(Runtime.getRuntime().availableProcessors()));
    }


    @Test
    public void testInitialElection() throws InterruptedException {
        int servers = 3;
        NodeConfig config1 = new NodeConfig(8258,
                new String[]{"localhost:8259", "localhost:8260"});
        NodeConfig config2 = new NodeConfig(8259,
                new String[]{"localhost:8258", "localhost:8260"});
        NodeConfig config3 = new NodeConfig(8260,
                new String[]{"localhost:8259", "localhost:8258"});


        // start nodes
        Node[] nodes = {new Node(config1), new Node(config2), new Node(config3)};
        Thread[] nodeThreads = {new Thread(nodes[0]), new Thread(nodes[1]), new Thread(nodes[2])};
        for(int i = 0; i < servers; i++) {
            nodeThreads[i].start();
        }

        // run some test
        for(int iter = 0; iter < 10; iter++) {
            // wait some time
            long ms = NodeConfig.HEARTBEAT_INTERVAL_MS+ ThreadLocalRandom.current().nextInt(100);
            TimeUnit.MILLISECONDS.sleep(ms);

            // check a leader exist
            int numLeader = 0;
            for(Node node: nodes) {
                if (node.getState().equals(RaftState.LEADER)) {
                    numLeader = 1;
                }
            }
            Assert.assertEquals(1, numLeader);
        }

        // destroy
        for(int i = 0; i < servers; i++) {
            nodeThreads[i].interrupt();
            nodes[i].destroy();
        }
    }
}
