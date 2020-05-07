package raft.consensusmodule;

import raft.Consensus;
import raft.nodemodule.Node;
import raft.statemachinemodule.RaftState;

/*
This implements Receiver Implementations
 */
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
        // Rules for servers
        if (args.term > this.nodehook.getCurrentTerm()) {
            // If RPC request or response contains term T > currentTerm, setCurrentTerm = T
            this.nodehook.setCurrentTerm(args.term);
            // convert to Follower
            this.nodehook.setState(RaftState.FOLLOWER);
        }

        // AppendEntries RPC
        // Reply false if term < currentTerm
        if (args.term < this.nodehook.getCurrentTerm()) {
            return new RaftAppendEntriesResult(this.nodehook.getCurrentTerm(), false);
        }

        // Reply false if log doesn't contain any entry at prevLogIndex whose term matches prevLogTerm
        if (this.nodehook.getLogModule().getLog(args.prevLogIndex) == null ||
                this.nodehook.getLogModule().getLog(args.prevLogIndex).term != args.prevLogTerm
        ) {
            return new RaftAppendEntriesResult(this.nodehook.getCurrentTerm(), false);
        }

        /*
        TODO:
         If an existing entry conflicet with a new one
         (Same index but different terms)
         delete the existing entries and all that follow it.
         (I believe this is to do wil log replication. We are currently doling Leader election)
         */

        /*
        TODO:
        Append new entries not already in the log
         */

        // if leaderCommit > commitINdex, set commitIndex = min(leaderCommit, index of alst new entry)
        if (args.leaderCommit > this.nodehook.getCommitIndex()) {
            this.nodehook.setCommitIndex(Math.min(args.leaderCommit, this.nodehook.getLogModule().getLastIndex()));
        }

        // true if follow contained entry matching prevLogIndex and prevLogTerm
        return new RaftAppendEntriesResult(this.nodehook.getCurrentTerm(), true);
    }
}
