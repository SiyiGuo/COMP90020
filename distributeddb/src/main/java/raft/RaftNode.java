package raft;

public class RaftNode {
    private RaftState state;

    public RaftNode() {

    }

    public Result requestVoteRpc(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        return null;
    }

    public Result appendEntriedRpc(int term, int leaderId, int prevLogIndex, int prevLogTerm, int[] entried, int leaderCommit) {
        return null;
    }

    public RaftState getState() {
        return this.state;
    }
}
