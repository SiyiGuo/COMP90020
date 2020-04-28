package raft.rpcmodule;

import io.grpc.stub.StreamObserver;
import raft.nodemodule.Node;
import raft.rpcmodule.requestvote.RequestVoteRequest;
import raft.rpcmodule.requestvote.RequestVoteResponse;
import raft.rpcmodule.requestvote.RequestVoteServiceGrpc;

public class RequestVoteImpl extends RequestVoteServiceGrpc.RequestVoteServiceImplBase {
    Node nodeHook;
    public RequestVoteImpl(Node nodehook) {
        super();
        this.nodeHook = nodehook;
    }

    @Override
    public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver) {
        RequestVoteResponse response = RequestVoteResponse.newBuilder()
                .setTerm(request.getTerm())
                .setVoteGranted(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
