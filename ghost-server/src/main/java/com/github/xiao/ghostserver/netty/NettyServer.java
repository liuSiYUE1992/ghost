package com.github.xiao.ghostserver.netty;

import com.github.xiao.ghostserver.util.TypeEnum;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public interface NettyServer {

    /**
     * 启动 netty 服务
     * @param initHandler 初始化handler
     * @param port 端口
     * @return ChannelFuture
     */
    ChannelFuture startServer (ChannelHandler initHandler,int port);

    /**
     * 获取 type
     * @return TypeEnum
     */
     default TypeEnum choseType(){
        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("linux") &&  Epoll.isAvailable()){
            return TypeEnum.EPOLL;
        }else if(osName.contains("mac") && KQueue.isAvailable()){
            return TypeEnum.KQUEUE;
        }else {
            return TypeEnum.SELECT;
        }
    }

    /**
     * 获取线程数
     * @return int
     */
    default int getThreadNumber(){
        return    Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    }

}
