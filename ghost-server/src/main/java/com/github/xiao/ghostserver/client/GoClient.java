package com.github.xiao.ghostserver.client;

import io.netty.channel.Channel;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public class GoClient implements GhostClient{


    private final Long createTime;


    private  String tag;

    private Integer proxyPort;


    private Channel channel;


    public GoClient() {
        this.createTime = System.nanoTime();
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public Long getCreateTime() {
        return createTime;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel =  channel;
    }

    @Override
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    @Override
    public Integer getProxyPort() {
        return proxyPort;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
    }
}
