package raft.rpcmodule;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.rpcmodule.requestvote.RequestVoteRequest;
import raft.rpcmodule.requestvote.RequestVoteResponse;
import raft.rpcmodule.requestvote.RequestVoteServiceGrpc;

import java.util.concurrent.TimeUnit;

public class RequestVoteClient {
    private final ManagedChannel channel;
    private final RequestVoteServiceGrpc.RequestVoteServiceBlockingStub blockingStub;

    public RequestVoteClient(String host, int hostPort) {
        channel = ManagedChannelBuilder.forAddress(host, hostPort)
                .usePlaintext()
                .build();
        blockingStub = RequestVoteServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public RaftRequestVoteResult requestVote(RaftRequestVoteArgs args) {
        // correspong to name field
        RequestVoteRequest request = RequestVoteRequest.newBuilder()
                .setTerm(args.term)
                .setCandidateId(args.candidateId)
                .setLastLogIndex(args.lastLogIndex)
                .setLastLogTerm(args.lastLogTerm.term)
                .build();
        // send out the response here

        // TODO: blockingStub = no parallel action?
        RequestVoteResponse response = blockingStub.requestVote(request);
        return new RaftRequestVoteResult(response.getTerm(), response.getVoteGranted());
    }
}
