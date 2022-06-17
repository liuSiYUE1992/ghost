package com.github.xiao.ghostserver.netty;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public class ServerThreadFactory implements ThreadFactory {
    private final String groupName;

    public ServerThreadFactory(String groupName) {
        this.groupName = groupName;
    }

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(groupName + "-" + threadNumber.incrementAndGet());
        return thread;
    }
}
