package com.github.xiao.ghostserver.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public class MessageQueue extends LinkedBlockingQueue<Object> {

    private final static Map<String,MessageQueue> QUEUES = new ConcurrentHashMap<>();
    public synchronized static MessageQueue getQueue(String port){
        MessageQueue queue = QUEUES.get(port);
        if(queue == null){
            queue = new MessageQueue();
            QUEUES.put(port,queue);
            return queue;
        }
        return queue;
    }
}
