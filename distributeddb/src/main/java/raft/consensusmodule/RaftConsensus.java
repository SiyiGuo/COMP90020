package raft.consensusmodule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.Consensus;
import raft.logmodule.RaftLogEntry;
import raft.nodemodule.Node;
import raft.statemachinemodule.RaftState;
import raft.ruleset.RulesForServers;

import java.util.ArrayList;

/*
This implements Receiver Implementations
Request Handling
 */
public class RaftConsensus implements Consensus {
    public final static Logger logger = LogManager.getLogger(Consensus.class);
    private volatile Node node;
    public RaftConsensus(Node nodehook) {
        this.node = nodehook;
    }

    @Override
    public RaftRequestVoteResult handleRequestVote(RaftRequestVoteArgs args) {
        // TODO: handle new joiner requestVote command
        // TODO: when recieved unknown request, add it to addressBook?
        // OR: send request to leader as registration

        RulesForServers.compareTermAndBecomeFollower(args.term, this.node);

        /*
        RequestVote RPC
        Receiver Implementation
         */
        // Reply false if term < currentTerm;
        if (args.term < this.node.getCurrentTerm()) {
            return new RaftRequestVoteResult(
                    this.node.getCurrentTerm(),
                    false
            );
        }

        // if votedFor is null or candidateId
        if (this.node.getVotedFor() == Node.NULL_VOTE || this.node.getVotedFor() == args.candidateId) {
            // and candidate's log is at least as up-todate as receiver's log
            if (args.lastLogIndex >= this.node.getLogModule().getLastIndex()) {
                // grand vote
                this.node.setVotedFor(args.candidateId);
                return new RaftRequestVoteResult(
                        this.node.getCurrentTerm(),
                        true
                );
            }
        }

        return new RaftRequestVoteResult(this.node.getCurrentTerm(), false);
    }

    @Override
    public RaftAppendEntriesResult handleAppendEntries(RaftAppendEntriesArgs args) {
        RulesForServers.compareTermAndBecomeFollower(args.term, this.node);

        // Candidates (§5.2): If AppendEntries RPC received from new leader: convert to follower
        if (this.node.getState() == RaftState.CANDIDATE) {
            this.node.setState(RaftState.FOLLOWER);
        }

        /*
        AppendEntries RPC
        Receiver Implementation. Should be Triggered by FOLLOWER
         */
        if (this.node.getState() == RaftState.FOLLOWER) {
            // Reply false if term < currentTerm
            if (args.term < this.node.getCurrentTerm()) {
                return new RaftAppendEntriesResult(this.node.getCurrentTerm(), false);
            }

            // set last hearthbeat time
            this.node.setLastElectionTime(System.currentTimeMillis());
            this.node.setRandomTimeout();

            // Reply false if log doesn't contain any entry at prevLogIndex whose term matches prevLogTerm
            if (this.node.getLogModule().getLog(args.prevLogIndex) == null ||
                    this.node.getLogModule().getLog(args.prevLogIndex).term != args.prevLogTerm
            ) {
                return new RaftAppendEntriesResult(this.node.getCurrentTerm(), false);
            }

            /*
             If an existing entry conflict with a new one
             (Same index but different terms)
             delete the existing entries and all that follow it.
             */
            ArrayList<RaftLogEntry> newEntries = new ArrayList<>();
            for(RaftLogEntry newEntry: args.entries) {
                RaftLogEntry existingEntry = this.node.getLogModule().getLog(newEntry.index);
                if (existingEntry != null && existingEntry.term != newEntry.term) {
                    // There is a conflict. delete existing entry and all that follow it
                    this.node.getLogModule().removeOnStartIndex(newEntry.index);
                } else {
                    newEntries.add(newEntry);
                }
            }

            //Append new entries not already in the log
            newEntries.forEach((newEntry) -> {
                this.node.getLogModule().append(newEntry);
            });

            // if leaderCommit > commitINdex, set commitIndex = min(leaderCommit, index of alst new entry)
            if (args.leaderCommit > this.node.getCommitIndex()) {
                this.node.setCommitIndex(Math.min(args.leaderCommit, this.node.getLogModule().getLastIndex()));
            }

            // true if follow contained entry matching prevLogIndex and prevLogTerm
            return new RaftAppendEntriesResult(this.node.getCurrentTerm(), true);
        }


        if (this.node.getState() == RaftState.LEADER) {
            /*
            TODO:
            If command received from client: append entry to local log,
            respond after entry applied to state machine (§5.3)
            • If last log index ≥ nextIndex for a follower: send
            AppendEntries RPC with log entries starting at nextIndex
            • If successful: update nextIndex and matchIndex for
            follower (§5.3)
            • If AppendEntries fails because of log inconsistency:
            decrement nextIndex and retry (§5.3)
            • If there exists an N such that N > commitIndex, a majority
            of matchIndex[i] ≥ N, and log[N].term == currentTerm:
            set commitIndex = N (§5.3, §5.4).
             */

            return new RaftAppendEntriesResult(this.node.getCurrentTerm(), true);
        }

        // Should not be triggered
        logger.warn("Node {} should not reach this when handling AppendEntries {}",
                this.node.nodeId, this.node.toString());
        return new RaftAppendEntriesResult(this.node.getCurrentTerm(), false);
    }
}
