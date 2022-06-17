package com.github.xiao.ghostserver.client;

import io.netty.channel.Channel;

/**
 * @author wang xiao
 * @date 2022/6/17
 */
public interface GhostClient {


    /**
     * 获取创建时间
     * @return 创建时间
     */
    Long  getCreateTime();


    /**
     * 设置唯一标签
     * @param tag  String
     */
    void  setTag(String tag);

    /**
     * 获取唯一标签
     * @return String
     */
    String   getTag();


    Channel getChannel();


    void setChannel(Channel channel);


    void  setProxyPort(Integer proxyPort);


    Integer getProxyPort();

}
