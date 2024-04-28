package com.project.chatting;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerMessageReader implements Runnable {

    private BufferedReader in;
    public ServerMessageReader(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String serverMsg;
            while ((serverMsg = in.readLine()) != null) {
                System.out.println(serverMsg);  // 서버로부터 받은 메시지 출력
            }
        } catch (IOException e) {
            // 서버 닫았을 때 클라이언트에게 출력되는 메시지
            System.out.println("Server connection was closed.");
        }
    }
}
