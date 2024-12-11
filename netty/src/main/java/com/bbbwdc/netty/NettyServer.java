package com.bbbwdc.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NettyServer {
    public static void main(String[] args) {
        NettyServer nettyServer = new NettyServer();
        // 指定服务端端口
        nettyServer.start(8088);
    }

    public void start(int port) {
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

                            // 将OutBoundHandler放在后面
//                            pipeline.addLast(new NettyServerInBoundHandler());
//                            pipeline.addLast(new NettyServerOutBoundHandler1());
//                            pipeline.addLast(new NettyServerOutBoundHandler2());

                            // 将OutBoundHandler放在前面
                            pipeline.addFirst(new NettyServerOutBoundHandler1());
                            pipeline.addFirst(new NettyServerOutBoundHandler2());
                            pipeline.addLast(new NettyServerInBoundHandler());

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

    /**
     * 自定义一个 Handler，需要继承 Netty 规定好的某个 HandlerAdapter（规范）
     * InboundHandler 用于处理数据流入本端（服务端）的 IO 事件
     * OutboundHandler 用于处理数据流出本端（服务端）的 IO 事件
     */
    static class NettyServerInBoundHandler extends ChannelInboundHandlerAdapter {
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // SocketChannel准备好的时候回调这个函数
            System.out.println("NettyServerInBoundHandler channelActive");
            super.channelActive(ctx);
        }

        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // SocketChannel断开连接的时候回调这个函数
            System.out.println("NettyServerInBoundHandler channelInactive");
            super.channelInactive(ctx);
        }

        /**
         * 当通道有数据可读时执行
         *
         * @param ctx 当前handler的上下文对象，可以从中取得相关联的 Pipeline、Channel、客户端地址等
         * @param msg 客户端发送的数据
         * @throws Exception
         */
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("NettyServerInBoundHandler channelRead");
            // msg其实是一个ByteBuf对象，Reactor中的缓冲区是ByteBuffer, netty中的缓冲区是ByteBuf
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            String content = new String(bytes, Charset.defaultCharset());
            System.out.println("收到的数据" + content);

            super.channelRead(ctx, msg);
        }

        /**
         * 数据读取完毕后执行
         *
         * @param ctx 上下文对象
         * @throws Exception
         */
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("NettyServerInBoundHandler channelReadComplete");
            // 向客户端写回数据
            Channel channel = ctx.channel();
            // 写回数据也是要放到ByteBuf里面的
            // 分配一个ByteBuf
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeBytes("Hello, Netty Client".getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(buffer);
            // 如果使用context写回数据，事件会从当前handler流向头部，如果这个handler后面还有outboundHandler，那么outboundHandler不会执行
//            ctx.writeAndFlush(buffer);
            super.channelReadComplete(ctx);

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("NettyServerInBoundHandler exceptionCaught," + cause.getMessage());
            super.exceptionCaught(ctx, cause);
        }
    }

    static class NettyServerOutBoundHandler1 extends ChannelOutboundHandlerAdapter {

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("ServerOutboundHandler1 " + ((ByteBuf) msg).toString(StandardCharsets.UTF_8));
            super.write(ctx, msg, promise);
        }
    }

    static class NettyServerOutBoundHandler2 extends ChannelOutboundHandlerAdapter {

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("ServerOutboundHandler2 " + ((ByteBuf) msg).toString(StandardCharsets.UTF_8));
            super.write(ctx, msg, promise);
        }
    }
}