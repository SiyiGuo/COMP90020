package raft.consensusmodule;

import raft.Consensus;
import raft.nodemodule.Node;
import raft.statemachinemodule.RaftState;

public class RaftConsensus implements Consensus {
    private volatile Node nodehook;
    public RaftConsensus(Node nodehook) {
        this.nodehook = nodehook;
    }

    @Override
    public RaftRequestVoteResult requestVote(RaftRequestVoteArgs args) {
        // Rules for servers
        if (args.term > this.nodehook.getCurrentTerm()) {
            // If RPC request or response contains term T > currentTerm, setCurrentTerm = T
            this.nodehook.setCurrentTerm(args.term);
            // convert to Follower
            this.nodehook.setState(RaftState.FOLLOWER);
        }

        // RequestVote RPC
        // Reply false if term < currentTerm
        if (args.term < this.nodehook.getCurrentTerm()) {
            return new RaftRequestVoteResult(
                    this.nodehook.getCurrentTerm(),
                    false
            );
        }

        // if votedFor is null or candidateId
        if (this.nodehook.getVotedFor() == Node.NULL_VOTE || this.nodehook.getVotedFor() == args.candidateId) {
            // and candidate's log is at least as up-todate as receiver's log
            if (args.lastLogIndex >= this.nodehook.getLogModule().getLastIndex()) {
                // grand vote
                this.nodehook.setVotedFor(args.candidateId);
                return new RaftRequestVoteResult(
                        this.nodehook.getCurrentTerm(),
                        true
                );
            }
        }

        return new RaftRequestVoteResult(this.nodehook.getCurrentTerm(), false);
    }

    @Override
    public RaftAppendEntriesResult appendEntries(RaftAppendEntriesArgs args) {
        return null;
    }
}
