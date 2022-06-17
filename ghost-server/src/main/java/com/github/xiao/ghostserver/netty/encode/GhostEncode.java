package com.github.xiao.ghostserver.netty.encode;

import com.github.xiao.ghostserver.netty.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public class GhostEncode extends MessageToByteEncoder<Payload> {

    protected void encode(ChannelHandlerContext channelHandlerContext, Payload payload, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(payload.getFlag());
        byteBuf.writeInt(payload.getType());
        byteBuf.writeInt(payload.getLength());

        if (null != payload.getData()){
            byteBuf.writeBytes(payload.getData());
        }
        byteBuf.writeInt(payload.getCrc());

    }
}
