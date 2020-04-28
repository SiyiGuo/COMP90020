package raft.consensusmodule;

import raft.Consensus;

public class RaftConsensus implements Consensus {

    @Override
    public RaftRequestVoteResult requestVote(RaftRequestVoteArgs args) {
        return null;
    }

    @Override
    public RaftAppendEntriesResult appendEntries(RaftAppendEntriesArgs args) {
        return null;
    }
}
