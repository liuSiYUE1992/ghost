package com.github.xiao.ghostserver.netty;

import com.github.xiao.ghostserver.util.Const;

import java.io.Serializable;

/**
 * 消息
 * @author wang xiao
 * @date 2022/6/17
 */
public class Payload implements Serializable {

    private  int flag;

    private int type;

    private int length;


    private byte[] data;


    private int crc;


    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public static Payload createJustType(int type) {
        Payload payload = new Payload();
        payload.setFlag(Const.FLAG);
        payload.setType(type);
        payload.setLength(0);
        return payload;
    }
}
