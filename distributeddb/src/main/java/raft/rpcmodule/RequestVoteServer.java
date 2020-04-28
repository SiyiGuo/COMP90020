package raft.rpcmodule;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RequestVoteServer {
    private int listenPort;
    private Server server;
    final static Logger logger = LogManager.getLogger(RequestVoteServer.class);

    public RequestVoteServer(int listenPort) {
        this.listenPort = listenPort;
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(this.listenPort)
                .addService((BindableService) new RequestVoteImpl())
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
}
