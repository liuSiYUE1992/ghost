package com.github.xiao.ghostserver.netty;

import com.github.xiao.ghostserver.netty.decode.GhostDecode;
import com.github.xiao.ghostserver.netty.encode.GhostEncode;
import com.github.xiao.ghostserver.netty.handler.GhostServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final boolean sync;


    public ServerInitializer(boolean sync){
        this.sync = sync;
    }
    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,12,4,0,0));
        ch.pipeline().addLast(new GhostEncode());
        ch.pipeline().addLast(new GhostDecode());
        ch.pipeline().addLast(new GhostServerHandler(sync));
    }
}
