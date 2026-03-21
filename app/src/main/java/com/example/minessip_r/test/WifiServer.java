package com.example.minessip_r.test;


import static com.example.minessip_r.Constants.DATA_DIRECTORY;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class WifiServer {

    public static void startService(){
        ServerSocket serverSocket = null;

        try {
            // 创建服务端Socket
            serverSocket = new ServerSocket(4321);
            Log.d("WifiServer","服务端启动，等待客户端连接...");

            while (true) {
                // 等待客户端连接
                Socket clientSocket = serverSocket.accept();
                Log.d("WifiServer","客户端已连接: " + clientSocket.getInetAddress());

                // 创建输入流和输出流
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                // 读取客户端发送的数据
                String receivedMessage;
                while ((receivedMessage = reader.readLine()) != null) {
                    Log.d("WifiServer","收到消息: " + receivedMessage);

                    // 根据接收到的数据决定响应行为
                    if ("trigger".equals(receivedMessage.trim())) {
                        sendFileData(writer);
                    }
                }

                // 关闭客户端连接
                reader.close();
                writer.close();
                clientSocket.close();
                System.out.println("客户端已断开连接");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("WifiServer","Exception: " + e);
        } finally {
            // 关闭服务端Socket
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendFileData(BufferedWriter writer) {
        File directory = new File(DATA_DIRECTORY);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    // 只处理 .dat 文件
                    return name.endsWith(".dat");
                }
            });

            if (files != null && files.length > 0) {
                // 遍历每个 .dat 文件并发送数据
                for (File file : files) {
                    try {
                        Log.d("WifiServer", "读取文件: " + file.getAbsolutePath());

                        // 使用字节流读取文件
                        FileInputStream fileInputStream = new FileInputStream(file);
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                        byte[] buffer = new byte[1024];  // 设置一个缓冲区
                        int bytesRead;
                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                            // 将读取到的字节发送给客户端
                            writer.write(new String(buffer, 0, bytesRead));  // 这里将字节转换为字符串进行发送
                            writer.flush();
                        }
                        bufferedInputStream.close();
                        Log.d("WifiServer", "文件发送完毕: " + file.getName());
                    } catch (IOException e) {
                        Log.e("WifiServer", "读取文件时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                Log.d("WifiServer", "没有找到任何 .dat 文件");
            }
        } else {
            Log.e("WifiServer", "文件夹不存在或不可访问: " + DATA_DIRECTORY);
        }
    }


}
