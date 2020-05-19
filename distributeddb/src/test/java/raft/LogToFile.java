package raft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import raft.logmodule.RaftLogEntry;
import raft.statemachinemodule.RaftCommand;

import application.storage.JsonLogStorage;
import application.storage.LogStorage;

import org.junit.Test;
import org.junit.BeforeClass;

import java.util.logging.Level;

public class LogToFile {

    final static Logger logger = LogManager.getLogger(RaftTest.class);

    @BeforeClass
    public static void beforeClass() {
        logger.debug("start testing log storage");
        java.util.logging.Logger.getLogger("io.grpc.netty.shaded.io.grpc.netty.NettyClientHandler").setLevel(Level.OFF);
    }

    @Test
    public void saveLog() {
        LogStorage storage = new JsonLogStorage();
        RaftLogEntry sample = new RaftLogEntry(0, 0, RaftCommand.GET, "", "");
        storage.add(System.currentTimeMillis(), sample);
    }
}
