package com.github.xiao.ghostserver.netty.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.xiao.ghostserver.client.GhostClient;
import com.github.xiao.ghostserver.client.GoClient;
import com.github.xiao.ghostserver.netty.Payload;
import com.github.xiao.ghostserver.util.MessageQueue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.github.xiao.ghostserver.util.Const.*;


/**
 * @author wang xiao
 * @date 2022/6/17
 */
@ChannelHandler.Sharable
public class GhostServerHandler extends SimpleChannelInboundHandler<Payload> {

    private final boolean sync;

    public static final Set<Channel>  ACTIVE_CHANNEL = new CopyOnWriteArraySet<>();

    public static volatile Map<String, GhostClient>  CLIENTS  = new ConcurrentHashMap<>();

    public static volatile Map<String, Set<byte[]>>  cache            = new ConcurrentHashMap<>();

    public GhostServerHandler(boolean sync) {
        this.sync = sync;
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Payload payload) throws Exception {
        int   type = payload.getType();
        switch (type){
            case CONN :
                register(payload,channelHandlerContext);
                break;
            case CLIENT_PUBLISH:
                JSONObject data = JSON.parseObject(payload.getData());
                String target = data.getString("target");
                String port = data.getString("port");
                if(sync){
                    MessageQueue.getQueue(port).add(payload.getData());
                }else if(!ProxyServerHandler.CHANNEL_MAP.containsKey(target + port)){
                    Set<byte[]> bytes = cache.get(target + port);
                    if(bytes == null || bytes.size() == 0){
                        bytes = new CopyOnWriteArraySet<>();
                        bytes.add(payload.getData());
                        cache.put(target + port,bytes);
                    }
                }else{
                    Set<byte[]> bytes = cache.get(target + port);
                    if(bytes!=null){
                        for (byte[] aByte : bytes) {
                            if(Arrays.equals(aByte, payload.getData())){
                                return;
                            }
                        }
                    }
                    ProxyServerHandler.CHANNEL_MAP.forEach((key, value) -> {
                        if (key.contains(target + port)) {
                            if(payload.getData()!=null){
                                value.writeAndFlush(payload.getData());
                            }else{
                                value.writeAndFlush("".getBytes());
                            }
                        }
                    });
                }
                break;
            default:
                break;
        }

    }


    /**
     * 注册一个客户端  name + port
     * @param message tcp协议
     * @param ctx 上下文
     */
    private void register(Payload message,ChannelHandlerContext ctx){
        GhostClient client = new GoClient();
        JSONObject data = JSON.parseObject(message.getData());
        Integer proxyPort = data.getInteger("proxyPort");
        String target = data.getString("target");
        client.setTag(target+proxyPort);
        client.setProxyPort(proxyPort);
        client.setChannel(ctx.channel());
        CLIENTS.put(client.getTag(),client);
        Payload justType = Payload.createJustType(CONN_ACK);
        ctx.channel().writeAndFlush(JSON.toJSONBytes(justType));
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ACTIVE_CHANNEL.add(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }
}
