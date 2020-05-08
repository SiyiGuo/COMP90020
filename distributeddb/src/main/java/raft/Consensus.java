package raft;

import raft.consensusmodule.RaftAppendEntriesArgs;
import raft.consensusmodule.RaftAppendEntriesResult;
import raft.consensusmodule.RaftRequestVoteArgs;
import raft.consensusmodule.RaftRequestVoteResult;

public interface Consensus {

    RaftRequestVoteResult handleRequestVote(RaftRequestVoteArgs args);

    RaftAppendEntriesResult handleAppendEntries(RaftAppendEntriesArgs args);
}
