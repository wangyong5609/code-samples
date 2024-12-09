package com.bbbwdc.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * NIO客户端
 */
public class NioClient {
    public static void main(String[] args) throws IOException {
        // 创建一个SocketChannel
        SocketChannel socketChannel = SocketChannel.open();
        // 连接服务端
        socketChannel.connect(new InetSocketAddress(8080));

        // 向服务端发送消息
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入消息：");

        while (true) {
            String message = scanner.nextLine();
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            // 客户端发送消息给服务端
            socketChannel.write(buffer);
            buffer.clear();

            // 读取服务端的回复
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int read = socketChannel.read(readBuffer);
            String response = new String(readBuffer.array()).trim();
            System.out.println("服务端回复：" + response);
        }
    }
}
