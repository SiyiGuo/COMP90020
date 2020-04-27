package raft;

import raft.consensusmodule.AppendEntriesArgs;
import raft.consensusmodule.AppendEntriesResult;
import raft.consensusmodule.RequestVoteArgs;
import raft.consensusmodule.RequestVoteResult;

public interface Consensus {

    RequestVoteResult requestVote(RequestVoteArgs args);

    AppendEntriesResult appendEntries(AppendEntriesArgs args);
}
