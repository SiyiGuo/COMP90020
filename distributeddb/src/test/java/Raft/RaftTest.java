package Raft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class RaftTest {
    final static Logger logger = LogManager.getLogger(RaftTest.class);

    @BeforeClass
    public static void beforeClass() {
        logger.debug("start testing");
    }
    @Test
    public void testInitialElection() {
        int servers = 3;
    }
}
