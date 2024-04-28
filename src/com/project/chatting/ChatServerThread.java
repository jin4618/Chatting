package com.project.chatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

public class ChatServerThread extends Thread {
    private Socket clientSocket;
    private String nickname;
    private Map<String, PrintWriter> chatClients;
    private BufferedReader in;
    PrintWriter out;

    public ChatServerThread(Socket clientSocket, Map<String, PrintWriter> chatClients) {
        this.clientSocket = clientSocket;
        this.chatClients = chatClients;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            nickname = in.readLine();
            broadcast(nickname + " 닉네임의 사용자가 연결하였습니다.");


            synchronized (chatClients) {
                chatClients.put(this.nickname, out);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        String msg;
        manual(nickname);
        try {
            while ((msg = in.readLine()) != null) {
                if (msg.equals("/bye")) {
                    System.out.println(nickname + " 닉네임의 사용자가 연결을 끊었습니다.");
                    break;
                }
                else
                    broadcast(nickname + " : " + msg);
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            synchronized (chatClients) {
                chatClients.remove(nickname);
            }
            broadcast(nickname + " 닉네임의 사용자가 연결을 끊었습니다.");

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void manual(String nickname) {
        PrintWriter out = chatClients.get(nickname);
        if (out != null) {
            out.println("채팅 사용 방법");
            out.println("방 목록 보기 : /list\n" +
                    "방 생성 : /create\n" +
                    "방 입장 : /join [방번호]\n" +
                    "방 나가기 : /exit\n" +
                    "접속종료 : /bye\n");
        }
    }

    public void broadcast(String msg) {
        synchronized (chatClients) {
            Iterator<PrintWriter> iterator = chatClients.values().iterator();
            while (iterator.hasNext()) {
                PrintWriter out = iterator.next();
                try {
                    out.println(msg);
                } catch (Exception e) {
                    iterator.remove();
                    e.printStackTrace();
                }
            }
        }
    }

}
