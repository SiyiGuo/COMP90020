package raft.rpcmodule;

import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.logmodule.LogEntry;
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
        logger.error("{} received election request from node{}", this.nodeHook.nodeId, request.getCandidateId());

        RaftRequestVoteResult result = this.nodeHook.handleRequestVote(new RaftRequestVoteArgs(
                request.getTerm(),
                request.getCandidateId(),
                request.getLastLogIndex(),
                new LogEntry(request.getLastLogTerm())
        ));

        RequestVoteResponse response = RequestVoteResponse.newBuilder()
                .setTerm(result.term)
                .setVoteGranted(result.voteGranted)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
