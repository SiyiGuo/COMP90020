package raft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
for raft algorithm unittest
*/
public class RaftConfig {
    /*
    Extra Stuff
     */
    final static Logger logger = LogManager.getLogger(RaftConfig.class);
    final Random rand = new Random();

    /*
    From mit 6.824
     */
    RaftNode[] rafts;
    String[] applyErr; // from apply channel readers
    boolean[] connected;
    RaftPersister[] saved;
    String[][] endnames; // the port filenames each sends to
    ArrayList<HashMap<Integer, RaftLog>> logs; // copy of each server's(RaftNode) committed entries
    long start; // time at which RaftConfig() was created
    //being/end()statistics
    long t0; //time at which test called config.begin()
    int rpcs0; //rptTotal() at start of test
    int cmds0; // number of agreements
    long bytes0;
    int maxIndex;
    int maxIndex0;
    int n;




    public RaftConfig(int n) {
        this.n = n;
        this.applyErr = new String[n];
        this.rafts = new RaftNode[n];
        for(int i = 0; i < n; i++) {
            rafts[i] = new RaftNode();
        }
        this.connected = new boolean[n];
        this.saved = new RaftPersister[n];
        for(int i = 0; i < n; i++) {
            saved[i] = new RaftPersister();
        }
        this.endnames = new String[n][];
        this.logs = new ArrayList<HashMap<Integer, RaftLog>>();
        for(int i = 0; i < n; i++) {
            this.logs.add(new HashMap<Integer, RaftLog>());
        }
        this.start = Instant.now().getEpochSecond();

        // create a set of raft node
        for (int i = 0; i < this.n; i++) {
            this.start1(i);
        }

        // connect everyone
        for (int i = 0; i < this.n; i++) {
            this.connect1(i);
        }
    }

    public void start1(int nodeIndex) {
        // start a raft node
        // if one already there, crash 1
        this.crash1(nodeIndex);
    }

    public void connect1(int nodeIndex) {
        // connect a raft node to the network
    }

    public void crash1(int nodeINdex) {
        ;;
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
