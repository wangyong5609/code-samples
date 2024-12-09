package com.bbbwdc.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * NIO服务端
 */
public class NioServer {
    public static void main(String[] args) throws IOException {
        // 启动多路复用器
        Selector selector = Selector.open();
        // 打开服务端的通道，监听 8080端口
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        // 将通道注册到多路复用器上，并监听接收事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端开始监听8080端口...");

        while (true) {
            // 等待一秒，没有事件发生
            if (selector.select(1000) == 0) {
                continue;
            }
            // 获取事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 移除避免重复处理
                iterator.remove();
                // 有连接事件
                if (key.isAcceptable()) {
                    // 有新的连接
                    SocketChannel channel = serverSocketChannel.accept();
                    channel.configureBlocking(false);
                    // 将socketChannel 也注册到selector，关注读事件，并给socketChannel关联buffer
                    channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    System.out.println("有新的连接, 注册到 selector，监听读事件...");
                } else if (key.isReadable()) {
                    // 有可读事件，说明客户端发送数据过来了
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    int read = channel.read(buffer);

                    if (read == -1) {
                        channel.close();
                        System.out.println("客户端关闭连接...");
                    } else {
                        String in = new String(buffer.array()).trim();
                        System.out.println("服务端收到消息：" + in);

                        String response = handleClientMessage(in);
                        ByteBuffer wBuffer = ByteBuffer.wrap(response.getBytes());
                        channel.write(wBuffer);
                    }
                }
            }
        }
    }

    private static String handleClientMessage(String message) {
        switch (message.toLowerCase()) {
            case "hello":
                return "Hello, client!";
            case "how are you?":
                return "I'm just a server, but thanks for asking!";
            case "bye":
                return "Goodbye, client!";
            default:
                return "Unknown command: " + message;
        }
    }
}
