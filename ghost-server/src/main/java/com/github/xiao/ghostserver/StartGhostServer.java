package com.github.xiao.ghostserver;


import com.github.xiao.ghostserver.netty.NettyServer;
import com.github.xiao.ghostserver.netty.Payload;
import com.github.xiao.ghostserver.netty.ServerInitializer;
import com.github.xiao.ghostserver.netty.ServerThreadFactory;
import com.github.xiao.ghostserver.netty.handler.GhostServerHandler;
import com.github.xiao.ghostserver.netty.server.DefaultNettyServer;
import io.netty.channel.Channel;
import org.apache.commons.cli.*;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.github.xiao.ghostserver.util.Const.PING;

/**
 * 服务启动类
 * @author wangxiao
 */
public class StartGhostServer {

    private static final ScheduledThreadPoolExecutor THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1,new ServerThreadFactory("keepalive"));

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("h", false, "Help");
        options.addOption("port", true, "server port");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd    = null;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("options", options);
            } else {
                int port = Integer.parseInt(cmd.getOptionValue("port", "9675"));
                boolean sync = cmd.hasOption("sync");
                NettyServer nettyServer = new DefaultNettyServer();
                nettyServer.startServer(new ServerInitializer(sync),port);
                System.out.println("server started "+(sync?"sync":"")+" on port " + port);
                ping();
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


    }


    public static void ping() {
        THREAD_POOL_EXECUTOR.scheduleAtFixedRate(() -> {
            GhostServerHandler.CLIENTS.values().removeIf(client -> !client.getChannel().isActive());
            for (Channel channel : GhostServerHandler.ACTIVE_CHANNEL) {
                if (channel.isOpen() && channel.isActive()) {
                    channel.writeAndFlush(Payload.createJustType(PING));
                } else {
                    channel.close();
                }
            }
        }, 5, 3, TimeUnit.SECONDS);


    }


}


