package com.github.xiao.ghostserver.netty.server;

import com.github.xiao.ghostserver.netty.NettyServer;
import com.github.xiao.ghostserver.netty.ServerThreadFactory;
import com.github.xiao.ghostserver.util.TypeEnum;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.kqueue.KQueueEventLoopGroup;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public class DefaultNettyServer implements NettyServer {

    public synchronized ChannelFuture start(ChannelHandler initializer, int port){
        TypeEnum typeEnum = choseType();
        EventLoopGroup bossGroup ;
        EventLoopGroup workerGroup;
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
            bossGroup = new KQueueEventLoopGroup(getThreadNumber(), new ServerThreadFactory(typeEnum.namePrefix()+"boss"));
            workerGroup = new KQueueEventLoopGroup(getThreadNumber(), new ServerThreadFactory(typeEnum.namePrefix()+"work"));
            serverBootstrap.group(bossGroup, workerGroup).channel(typeEnum.channelClass());
            serverBootstrap.childHandler(initializer);
            ChannelFuture f = serverBootstrap.bind(port).sync();
            f.channel().closeFuture().addListener((ChannelFutureListener) future -> {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            });
            return f;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ChannelFuture startServer(ChannelHandler initHandler, int port) {
        return null;
    }


}
