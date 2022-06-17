package com.github.xiao.ghostserver.netty.handler;

import com.alibaba.fastjson2.JSONObject;
import com.github.xiao.ghostserver.client.GhostClient;
import com.github.xiao.ghostserver.netty.Payload;
import com.github.xiao.ghostserver.util.MessageQueue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.github.xiao.ghostserver.util.Const.PUBLISH;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
@ChannelHandler.Sharable
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    public final static Map<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
    private final String       port;
    private final boolean sync;
    private final String targetName;


    public ProxyServerHandler(String targetName,String port,boolean sync){
        this.port = port;
        this.sync = sync;
        this.targetName = targetName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if(!sync){
            CHANNEL_MAP.put(targetName+port,ctx.channel());
            if(GhostServerHandler.cache.containsKey(targetName+port)){
                Set<byte[]> bytes = GhostServerHandler.cache.get(targetName + port);
                if(bytes!=null && bytes.size() > 0){
                    for (byte[] aByte : bytes) {
                        ctx.channel().writeAndFlush(aByte);
                    }
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        if(ctx.channel().isActive()){
            ctx.channel().close();
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception{
        byte[] msg = (byte[]) data;
        GhostClient client = GhostServerHandler.CLIENTS.get(targetName+port);
        if(client == null){
            return;
        }
        Channel channel    = client.getChannel();
        if (channel != null) {
            Payload message = new Payload();
            message.setType(PUBLISH);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("port", port);
            message.setLength(jsonObject.toJSONString().getBytes(CharsetUtil.UTF_8).length);
            message.setData(msg);
            channel.writeAndFlush(message);
            if(this.sync){
                Object take = MessageQueue.getQueue(targetName+port).poll(10, TimeUnit.SECONDS);
                if(take == null){
                    ctx.close();
                }else{
                    ctx.channel().writeAndFlush(take);
                    ctx.close();
                }
            }
        }
    }


}
