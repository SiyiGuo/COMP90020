package raft;

import raft.consensusmodule.RaftAppendEntriesArgs;
import raft.consensusmodule.RaftAppendEntriesResult;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;

public interface Consensus {

    RaftRequestVoteResult requestVote(RaftRequestVoteArgs args);

    RaftAppendEntriesResult appendEntries(RaftAppendEntriesArgs args);
}
