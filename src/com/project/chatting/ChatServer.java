package com.project.chatting;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {
    public static void main(String[] args) {
        int portNumber = 12345; // 포트 번호
        try (ServerSocket server = new ServerSocket(portNumber);) { // 서버 소켓 생성
            System.out.println("서버 준비 완료");
            Map<String, PrintWriter> chatClients = new HashMap<>(); // 클라이언트 정보 저장
            Map<Integer, List<String>> chatRoom = new HashMap<>();

            while (true) {  // 여러 명 통신
                Socket clientSocket = server.accept();    // 클라이언트 소켓 얻어옴

                System.out.println(clientSocket.getInetAddress().getHostAddress() + "로부터 연결되었습니다.");
                // 사용자 IP 주소 출력

                new ChatThread(clientSocket, chatClients, chatRoom).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
