package raft.rpcmodule;

import io.grpc.stub.StreamObserver;
import raft.rpcmodule.requestvote.RequestVoteRequest;
import raft.rpcmodule.requestvote.RequestVoteResponse;
import raft.rpcmodule.requestvote.RequestVoteServiceGrpc;

public class RequestVoteImpl extends RequestVoteServiceGrpc.RequestVoteServiceImplBase {
    @Override
    public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver) {
        RequestVoteResponse response = RequestVoteResponse.newBuilder().setMessage(("Response Message" + request.getName())).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
