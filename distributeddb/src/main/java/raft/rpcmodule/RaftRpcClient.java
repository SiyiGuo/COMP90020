package raft.rpcmodule;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.consensusmodule.RaftAppendEntriesArgs;
import raft.consensusmodule.RaftAppendEntriesResult;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.logmodule.RaftLogEntry;

import java.util.concurrent.TimeUnit;

/*
RaftAppendEntriesArgs
AppendEntriesRequest (RPC)
RaftRpcClient // adaptor to convert RPC class back to class
AppendEntriesResponse (RPC)
RaftAppendEntriesResult
 */
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

    public RaftRequestVoteResult requestVote(RaftRequestVoteArgs args) {
        // correspong to name field
        RequestVoteRequest request = RequestVoteRequest.newBuilder()
                .setTerm(args.term)
                .setCandidateId(args.candidateId)
                .setLastLogIndex(args.lastLogIndex)
                .setLastLogTerm(args.lastLogTerm)
                .build();
        // send out the response here

        // TODO: blockingStub = no parallel action?
        RequestVoteResponse response = blockingStub.requestVote(request);
        return new RaftRequestVoteResult(response.getTerm(), response.getVoteGranted());
    }

    public RaftAppendEntriesResult appendEntries(RaftAppendEntriesArgs args) {
        AppendEntriesRequest.Builder builder = AppendEntriesRequest.newBuilder();
        builder.setTerm(args.term)
                .setLeaderId(args.leaderId)
                .setPrevLogIndex(args.prevLogIndex)
                .setPrevLogTerm(args.prevLogTerm)
                .setLeaderCommit(args.leaderCommit);

        for (RaftLogEntry entry : args.entries) {
            LogEntry logEntry = LogEntry.newBuilder()
                    .setTerm(entry.term)
                    .setValue(entry.value)
                    .build();
            builder.addEntries(logEntry);
        }

        AppendEntriesRequest request = builder.build();
        AppendEntriesResponse response = blockingStub.appendEntries(request);
        return new RaftAppendEntriesResult(response.getTerm(), response.getSuccess());
    }
}
