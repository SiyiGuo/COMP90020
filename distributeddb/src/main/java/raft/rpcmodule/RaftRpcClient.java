package raft.rpcmodule;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;

import java.util.concurrent.TimeUnit;

public class RaftRpcClient {
    private final ManagedChannel channel;
    private final RaftRpcServiceGrpc.RaftRpcServiceBlockingStub blockingStub;
    private final Logger logger = LogManager.getLogger(RaftRpcClient.class);

    public RaftRpcClient(String host, int hostPort) {
        channel = ManagedChannelBuilder.forAddress(host, hostPort)
                .usePlaintext()
                .build();
        blockingStub = RaftRpcServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void test() {
        System.out.println("say something");
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
