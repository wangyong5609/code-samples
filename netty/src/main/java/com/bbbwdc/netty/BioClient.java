package com.bbbwdc.netty;

import java.io.*;
import java.net.Socket;

/**
 * BIO客户端
 */
public class BioClient {
    public static void main(String[] args) throws IOException {
        // 客户端连接服务端的8080端口
        Socket socket = new Socket("127.0.0.1", 8080);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // 向服务端发送消息
        bufferedWriter.write("你好，我是客户端" + "\n");
        // 刷新缓冲区
        bufferedWriter.flush();
        // 读取服务端的回复
        String line = bufferedReader.readLine();
        System.out.println("客户端收到消息：" + line);
    }
}
