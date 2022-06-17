package com.github.xiao.ghostserver.netty.decode;

import com.github.xiao.ghostserver.netty.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import static com.github.xiao.ghostserver.util.Const.*;


/**
 * @author wang xiao
 * @date 2022/6/17
 */
public class GhostDecode extends MessageToMessageDecoder<ByteBuf> {

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int flag = byteBuf.readInt();

        if (FLAG ==flag ){
            int type = byteBuf.readInt();
            if(type == PING || type == CONN_ACK){
                list.add(Payload.createJustType(type));
            }else{
                int length = byteBuf.readInt();
                if (byteBuf.readableBytes() >= length){
                    Payload payload = new Payload();
                    payload.setFlag(flag);
                    payload.setType(type);
                    byte [] bytes = new byte[length];
                    byteBuf.readBytes(bytes);
                    payload.setData(bytes);
                    int check = byteBuf.readInt();
                    payload.setCrc(check);
                    list.add(payload);
                }
            }
        }
    }
}

