package raft.concurrentutil;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author 莫那·鲁道
 * Taken from https://github.com/stateIs0/lu-raft-kv/blob/master/lu-raft-kv/src/main/java/cn/think/in/java/current/RaftThreadPool.java
 */
public class RaftThreadPool {

    private  int cup = Runtime.getRuntime().availableProcessors();
    private  int maxPoolSize = cup * 2;
    private  final int queueSize = 1024;
    private  final long keepTime = 1000 * 60;
    private  TimeUnit keepTimeUnit = TimeUnit.MILLISECONDS;

    private  ScheduledExecutorService ss = getScheduled();
    private  ThreadPoolExecutor te = getThreadPool();

    private  ThreadPoolExecutor getThreadPool() {
        return new RaftThreadPoolExecutor(
                cup,
                maxPoolSize,
                keepTime,
                keepTimeUnit,
                new LinkedBlockingQueue<>(queueSize),
                new NameThreadFactory());
    }

    private  ScheduledExecutorService getScheduled() {
        return new ScheduledThreadPoolExecutor(cup, new NameThreadFactory());
    }


    public  void scheduleAtFixedRate(Runnable r, long initDelay, long delay) {
        ss.scheduleAtFixedRate(r, initDelay, delay, TimeUnit.MILLISECONDS);
    }


    public  void scheduleWithFixedDelay(Runnable r, long delay) {
        ss.scheduleWithFixedDelay(r, 0, delay, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    public  <T> Future<T> submit(Callable r) {
        return te.submit(r);
    }

    public  void execute(Runnable r) {
        te.execute(r);
    }

    public  void execute(Runnable r, boolean sync) {
        if (sync) {
            r.run();
        } else {
            te.execute(r);
        }
    }

     class NameThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new RaftThread("Raft thread ", r);
            t.setDaemon(true);
            t.setPriority(5);
            return t;
        }
    }

}