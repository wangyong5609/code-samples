package com.bbbwdc.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyServer {
    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer();
        nettyServer.start(8080);
    }
    
    static void start(int port) {
        // 使用Reactor主从多线程模式，准备 Boos 和 worker
        NioEventLoopGroup boos = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            // 核心引导类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    // 设置父子线程组
                    .group(boos, worker)
                    // 说明服务端通道的实现类（便于netty做反射处理）
                    .channel(NioServerSocketChannel.class)
                    // handler()方法用于给 BossGroup 设置业务处理器
                    // childHandler()方法用于给 WorkerGroup 设置业务处理器
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 创建一个通道初始化对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 这里方法是有客户端新的连接过来,Channel初始化时才会回调
                            ChannelPipeline pipeline = ch.pipeline();
                        }
                    });
            // 绑定端口启动
            ChannelFuture future = serverBootstrap.bind(port).sync();
            // 监听端口的关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            worker.shutdownGracefully();
            boos.shutdownGracefully();
        }
    }
}
