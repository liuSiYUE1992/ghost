package com.github.xiao.ghostserver.util;

import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public enum TypeEnum {

    /**
     * epoll
     */
    EPOLL(){
        @Override
        public Class<? extends ServerSocketChannel> channelClass() {
            return EpollServerSocketChannel.class;
        }

        @Override
        public String namePrefix() {
            return "epoll";
        }
    },
    /**
     * Kqueue
     */
    KQUEUE(){
        @Override
        public Class<? extends ServerSocketChannel> channelClass() {
            return KQueueServerSocketChannel.class;
        }

        @Override
        public String namePrefix() {
            return "kqueue";
        }
    },

    /**
     * select
     */
    SELECT(){
        @Override
        public Class<? extends ServerSocketChannel> channelClass() {
            return NioServerSocketChannel.class;
        }

        @Override
        public String namePrefix() {
            return "select";
        }
    };


   public abstract Class<? extends ServerSocketChannel> channelClass ();


    public abstract String namePrefix ();
}
