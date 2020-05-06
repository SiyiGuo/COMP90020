package raft.rpcmodule;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.nodemodule.Node;

import java.io.IOException;

public class RaftRpcServer implements Runnable {
    final static Logger logger = LogManager.getLogger(RaftRpcServer.class);
    private int listenPort;
    private Server server;
    private Node nodeHook;

    public RaftRpcServer(int listenPort, Node nodeHook) {
        this.listenPort = listenPort;
        this.nodeHook = nodeHook;
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(this.listenPort)
                .addService((BindableService) new RaftRpcServerImpl(this.nodeHook))
                .build()
                .start();
        logger.info("-------------------- server start, waiting for connection----------------");
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//
//                System.err.println("*** shutting down gRPC server since JVM is shutting down");
//                RequestVoteServer.this.stop();
//                System.err.println("*** server shut down");
//            }
//        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void run() {
        try {
            logger.info("------ Start server listen to port {} ------", this.listenPort);
            this.start();
            this.blockUntilShutdown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
