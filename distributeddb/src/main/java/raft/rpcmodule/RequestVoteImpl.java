package raft.rpcmodule;

import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.nodemodule.Node;
import raft.rpcmodule.requestvote.RequestVoteRequest;
import raft.rpcmodule.requestvote.RequestVoteResponse;
import raft.rpcmodule.requestvote.RequestVoteServiceGrpc;

public class RequestVoteImpl extends RequestVoteServiceGrpc.RequestVoteServiceImplBase {
    private volatile Node nodeHook;
    private final Logger logger = LogManager.getLogger(RequestVoteImpl.class);

    public RequestVoteImpl(Node nodehook) {
        super();
        this.nodeHook = nodehook;
    }

    @Override
    public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver) {
        this.nodeHook.rpcCount += 1;
        logger.error("***{} sending back", this.nodeHook.config.listenPort);
        RequestVoteResponse response = RequestVoteResponse.newBuilder()
                .setTerm(request.getTerm())
                .setVoteGranted(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
