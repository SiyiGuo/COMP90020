package raft.concurrentutil;

/*
Debug util for logging
 */
public class Cu {
    public static void debug(String st) {
        System.err.println(st);
    }

    public static void debug(Object st) {

        System.err.println(st);
    }
}
