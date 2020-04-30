package raft.concurrentutil;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 莫那·鲁道
 * Taken from https://github.com/stateIs0/lu-raft-kv/blob/master/lu-raft-kv/src/main/java/cn/think/in/java/current/SleepHelper.java
 */
public class SleepHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SleepHelper.class);

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }

    }

    public static void sleep2(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage());
        }

    }


}