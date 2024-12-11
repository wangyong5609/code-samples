package com.bbbwdc.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class NettyClient {
    public static void main(String[] args) {
        NettyClient nettyClient = new NettyClient();
        nettyClient.connect("127.0.0.1", 8088);
    }

    public void connect(String host, int port) {
        // 客户端只需要一个事件循环组，可以看做 BossGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建客户端的启动对象
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    // 设置线程组
                    .group(group)
                    // 说明客户端通道的实现类（便于 Netty 做反射处理）
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new NettyClientInboundHandler());
                        }
                    });
            System.out.println("client is ready...");

            ChannelFuture channelFuture = bootstrap.connect(host, port);
            // 对通道关闭进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }

    }

    static class NettyClientInboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // SocketChannel准备好的时候回调这个函数
            System.out.println("NettyClientInboundHandler channelActive");
            // 向服务器发送数据
            ctx.writeAndFlush(
                    // Unpooled 类是 Netty 提供的专门操作缓冲区的工具
                    // 类，copiedBuffer 方法返回的 ByteBuf 对象类似于
                    // NIO 中的 ByteBuffer，但性能更高
                    Unpooled.copiedBuffer(
                            "Hello, Netty Server!",
                            CharsetUtil.UTF_8
                    )
            );
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // SocketChannel断开连接的时候回调这个函数
            System.out.println("NettyClientInboundHandler channelInactive");
            super.channelInactive(ctx);
        }

        /**
         * 当通道有数据可读时执行
         *
         * @param ctx 当前handler的上下文对象，可以从中取得相关联的 Pipeline、Channel、客户端地址等
         * @param msg 客户端发送的数据
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("NettyClientInboundHandler channelRead");
            // msg其实是一个ByteBuf对象，Reactor中的缓冲区是ByteBuffer, netty中的缓冲区是ByteBuf
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            String content = new String(bytes, Charset.defaultCharset());
            System.out.println("收到的数据" + content);

            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            super.channelReadComplete(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }
    }
}