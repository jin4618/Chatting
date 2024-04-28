package com.project.chatting;/*
1. 서버 연결 및 닉네임 설정
- 클라이언트 12345 포트로 대기중인 ChatServer에 접속
- 서버 접속하면 사용자는 닉네입을 입력 받아 서버에 전송
- 서버는 사용자의 닉네임을 받고 "ㅇㅇㅇ 닉네임의 사용자가 연결하였습니다." 출력
- 클라이언트가 접속하면 서버는 사용자의 IP 주소를 출력

2. 메시지 수신 및 발신
- 클라이언트는 닉네임을 입력한 후부터 서버로부터 메시지를 한 줄씩 받아 화면에 출력
- 사용자가 메시지 입력하면 서버에 전송
- 사용자가 "/bye"를 입력하면 연결 종료, 서버도 "ㅇㅇㅇ닉네임의 사용자가 연결을 끊었습니다." 출력 후 종료
*/


import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    public static void main(String[] args) {
        int portNumber = 12345; // 포트 번호
        try (ServerSocket server = new ServerSocket(portNumber);) { // 서버 소켓 생성
            System.out.println("서버 준비 완료");
            Map<String, PrintWriter> chatClients = new HashMap<>(); // 클라이언트 정보 저장


            while (true) {  // 여러 명 통신
                Socket clientSocket = server.accept();    // 클라이언트 소켓 얻어옴

                System.out.println(clientSocket.getInetAddress().getHostAddress() + "로부터 연결되었습니다.");
                // 사용자 IP 주소 출력

                new ChatServerThread(clientSocket, chatClients).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
