package raft.rpcmodule;

import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.logmodule.RaftLogEntry;
import raft.nodemodule.Node;

public class RaftRpcServerImpl extends RaftRpcServiceGrpc.RaftRpcServiceImplBase {
    private volatile Node nodeHook;
    private final Logger logger = LogManager.getLogger(RaftRpcServerImpl.class);

    public RaftRpcServerImpl(Node nodehook) {
        super();
        this.nodeHook = nodehook;
    }

    @Override
    public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver) {
        this.nodeHook.rpcCount += 1;
        RaftRequestVoteResult result = this.nodeHook.handleRequestVote(new RaftRequestVoteArgs(
                request.getTerm(),
                request.getCandidateId(),
                request.getLastLogIndex(),
                request.getLastLogTerm()
        ));

        RequestVoteResponse response = RequestVoteResponse.newBuilder()
                .setTerm(result.term)
                .setVoteGranted(result.voteGranted)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
