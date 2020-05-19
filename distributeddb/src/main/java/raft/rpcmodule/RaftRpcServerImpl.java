package raft.rpcmodule;

import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.consensusmodule.RaftAppendEntriesArgs;
import raft.consensusmodule.RaftAppendEntriesResult;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;
import raft.logmodule.RaftLogEntry;
import raft.nodemodule.Node;
import raft.nodemodule.RaftClientRequest;
import raft.nodemodule.RaftClientResponse;
import raft.statemachinemodule.RaftCommand;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

// adapter server to forward handling back to RaftNode class.
public class RaftRpcServerImpl extends RaftRpcServiceGrpc.RaftRpcServiceImplBase {
    private final Logger logger = LogManager.getLogger(RaftRpcServerImpl.class);
    private volatile Node nodeHook;

    public RaftRpcServerImpl(@NotNull Node nodehook) {
        super();
        this.nodeHook = nodehook;
    }

    @Override
    public void handleClientRequest(ClientRequest request, StreamObserver<ClientResponse> responseObserver) {
        RaftClientResponse result = this.nodeHook.handleClientRequest(new RaftClientRequest(
                RaftCommand.valueOf(request.getCommand()),
                request.getKey(),
                request.getValue()
        ));
        ClientResponse response = ClientResponse.newBuilder()
                .setCommand(result.command.name())
                .setKey(result.key)
                .setResult(result.result)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
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

    @Override
    public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        List<RaftLogEntry> entries = new ArrayList<RaftLogEntry>();

        for (LogEntry entry : request.getEntriesList()) {
            entries.add(new RaftLogEntry(
                    entry.getTerm(), entry.getIndex(),
                    RaftCommand.valueOf(entry.getCommand()),
                    entry.getKey(), entry.getValue()));
        }

        entries.forEach(e->{System.out.println("receivedL: " + e.toString()) ;});

        RaftAppendEntriesResult result = this.nodeHook.handleAppendEntries(new RaftAppendEntriesArgs(
                request.getTerm(),
                request.getLeaderId(),
                request.getPrevLogIndex(),
                request.getPrevLogTerm(),
                entries,
                request.getLeaderCommit()
        ));

        AppendEntriesResponse response = AppendEntriesResponse.newBuilder()
                .setTerm(result.term)
                .setSuccess(result.success)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
