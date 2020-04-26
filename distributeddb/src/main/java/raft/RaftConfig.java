package raft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
for raft algorithm unittest
*/
public class RaftConfig {

    final static Logger logger = LogManager.getLogger(RaftConfig.class);
    final Random rand = new Random();
    RaftNode[] rafts;
    int n;
    boolean[] connected;

    public RaftConfig() {

    }

    public int checkOneLeader() throws InterruptedException {
        logger.debug("Check one leader elected");
        for (int iter = 0; iter < 10; iter++) {
            int ms = 450 + randInt(0, Integer.MAX_VALUE) % 100;
            TimeUnit.MILLISECONDS.sleep(ms);

            // leaders := make(map[int][]int)
            SortedMap<Integer, ArrayList<Integer>> leaders = new TreeMap<Integer, ArrayList<Integer>>();
            for (int i = 0; i < n; i++) {
                RaftState state = rafts[i].getState();
                if (!leaders.containsKey(state.term)) {
                    leaders.put(state.term, new ArrayList<Integer>());
                }
                leaders.get(state.term).add(i); // leaders[term] = append(leaders[term], i)
            }

            int lastTermWithLeader = -1;
            for(Map.Entry<Integer, ArrayList<Integer>> entry: leaders.entrySet()) {
                int term = entry.getKey();
                ArrayList<Integer> termLeaders = leaders.get(term);
                if (termLeaders.size() > 1) {
                    logger.fatal("Term %d has %d (>1) leaders", term, termLeaders.size());
                }
                if (term > lastTermWithLeader) {
                    lastTermWithLeader = term;
                }
            }

            if (leaders.size() != 0) {
                return leaders.get(lastTermWithLeader).get(0);
            }
        }
        logger.fatal("expected one leader, got none");
        return -1;
    }

    public int randInt(int min, int max) {
        int randomNum = this.rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
