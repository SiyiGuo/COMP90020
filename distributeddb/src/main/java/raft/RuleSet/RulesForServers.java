package raft.RuleSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raft.nodemodule.Node;
import raft.statemachinemodule.RaftState;

/*
A collection of rule applied acoording to page 4

Rule implementation Priority:
All Server > Candidate > Follower > Receiver Implementation > Leader

THis is because candidate can become follower when handling request
ANd only follow need to reply.

Recevier Implementation is only applied on follower.
 */
public class RulesForServers {
    public static Logger logger = LogManager.getLogger(RulesForServers.class);
    /*
    Rules for Servers
     */

    // If RPC request or response contains term T > currentTerm: set currentTerm = T, convert to follower (§5.1)
    public static boolean  compareTermAndBecomeFollower(long receivedTerm, Node node) {
        if (node.getCurrentTerm() < receivedTerm) {
            logger.debug("Node{} has outdated term {} received term {}. Become follower",
                    node.nodeId, receivedTerm, node.getCurrentTerm());

            node.setCurrentTerm(receivedTerm);
            node.setState(RaftState.FOLLOWER);

             //TODO: check whether we need this
            node.setVotedFor(Node.NULL_VOTE);
            return true;
        }
        return false;
    }

    /*
    Rules for Candidate
     */
}
