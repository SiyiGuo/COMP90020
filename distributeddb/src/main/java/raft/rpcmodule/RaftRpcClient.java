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
import raft.nodemodule.RaftClientRequest;
import raft.nodemodule.RaftClientResponse;
import raft.statemachinemodule.RaftCommand;

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

    public RaftClientResponse handleClientRequest(RaftClientRequest req) {
        ClientRequest request = ClientRequest.newBuilder()
                .setCommand(req.command.name())
                .setKey(req.key)
                .setValue(req.value)
                .build();
        ClientResponse response = blockingStub.handleClientRequest(request);
        return new RaftClientResponse(
                RaftCommand.valueOf(response.getCommand()),
                response.getKey(),
                response.getResult()
        );
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
            System.err.println("--------append entries built " + entry.toString());
            LogEntry logEntry = LogEntry.newBuilder()
                    .setTerm(entry.term)
                    .setIndex(entry.index)
                    .setKey(entry.key)
                    .setValue(entry.value)
                    .setCommand(entry.command.name())
                    .build();
            builder.addEntries(logEntry);
        }

        AppendEntriesRequest request = builder.build();
        AppendEntriesResponse response = blockingStub.appendEntries(request);
        return new RaftAppendEntriesResult(response.getTerm(), response.getSuccess());
    }
}
