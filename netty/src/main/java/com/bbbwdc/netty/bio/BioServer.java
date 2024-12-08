package com.bbbwdc.netty.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO服务端
 */
public class BioServer {
    public static void main(String[] args) throws IOException {
        // 服务端监听8080端口
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("服务端开始监听8080端口...");
        while (true) {
            // 阻塞等待客户端连接
            Socket socket = serverSocket.accept();
            System.out.println("拿到一个客户端连接...,socket=" + socket);
            // 处理这个连接
            new Thread(new ServerHandler(socket)).start();
        }
    }

    static class ServerHandler implements Runnable {
        private final Socket socket;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // 读取客户端发送过来的数据
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                // 读缓冲区
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                // 写缓冲区
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

                String line = bufferedReader.readLine();
                System.out.println("服务端收到消息：" + line);

                // 回复客户端
                bufferedWriter.write("我是服务端，我已收到你的消息" + "\n");
                bufferedWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
