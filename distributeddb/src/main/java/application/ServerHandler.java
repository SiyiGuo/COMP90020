package application;

import io.grpc.Server;
import raft.logmodule.RaftLogEntry;
import raft.nodemodule.Node;
import raft.nodemodule.RaftClientRequest;
import raft.nodemodule.RaftClientResponse;

public class ServerHandler {
    private volatile Node node;
    public ServerHandler(Node node) {
        this.node = node;
    }

    public RaftClientResponse handleClientRequest(RaftClientRequest req) {
        /*
        TODO:
        If command received from client.
         */
        if (this.node.nodeId == this.node.addressBook.getLeaderId()) {
            switch (req.command) {
                case GETSTORAGE:
                    String snapshot = "\n";
                    snapshot += this.node.getStateMachine().getStorage().getAllValue();
                    return new RaftClientResponse(req.command, req.key, snapshot);
                case GETALLLOGS:
                    String result = "";
                    for (RaftLogEntry entry: this.node.getLogModule().getAllLogs()) {
                        result += "\n" + entry;
                    }
                    return new RaftClientResponse(req.command, req.key, result);
                case FINDLEADER:
                    return new RaftClientResponse(req.command, req.key,
                            Integer.toString(this.node.addressBook.getLeaderId()));
                case GET:
                    return new RaftClientResponse(req.command, req.key,
                            this.node.getStateMachine().getString(req.key));
                default:
                    // Append entry to local log
                    RaftLogEntry clientEntry = new RaftLogEntry(
                            this.node.getCurrentTerm(),
                            this.node.getLogModule().getLastIndex()+1, //log is index from one
                            req.command,
                            req.key,
                            req.value
                    );
                    this.node.getLogModule().append(clientEntry);
                    return new RaftClientResponse(req.command, req.key, "success");
            }
        }
        return this.redirect(req);
    }

    public RaftClientResponse redirect(RaftClientRequest req) {
        int leaderId = this.node.addressBook.getLeaderId();
        return this.node.peers.get(leaderId).handleClientRequest(req);
    }
}
