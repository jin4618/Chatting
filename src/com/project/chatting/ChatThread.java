package com.project.chatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ChatThread extends Thread {
    private Socket clientSocket;
    private String nickname;
    private static int roomId = 0;
    private Map<String, PrintWriter> chatClients;
    private List<String> roomList = new ArrayList<>();
    private Map<Integer, List<String>> chatRoom;
    private BufferedReader in;
    PrintWriter out;

    public ChatThread(Socket clientSocket, Map<String, PrintWriter> chatClients, Map<Integer, List<String>> chatRoom) {
        this.clientSocket = clientSocket;
        this.chatClients = chatClients;
        this.chatRoom = chatRoom;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                nickname = in.readLine();
                if (chatClients.containsKey(nickname)) {
                    out.println("이미 사용 중입니다. 다시 입력해주세요.");
                } else {
                    broadcast(nickname + " 닉네임의 사용자가 연결하였습니다.");
                    break;
                }
            }
            synchronized (this.chatClients) {
                this.chatClients.put(nickname, out);
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
                } else if (msg.equals("/list")) {
                    roomListView();
                } else if (msg.equals("/create")) {
                    createRoom();
                } else if (msg.indexOf("/join") == 0) {
                    joinRoom(msg);
                } else if (msg.indexOf("/whisper") == 0) {
                    whisperMsg(msg);
                } else {
                    broadcast(nickname + " : " + msg);
                }
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
                    "귓속말 : /whisper [닉네임] [메시지]\n" +
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

    public void roomListView() {
        StringBuilder room = new StringBuilder();
        if (chatRoom.isEmpty()) {
            out.println("존재하는 방이 없습니다.");
            out.println("-------------------");
            manual(nickname);
        } else {
            out.println("======= 방 목록 =======");
            for (int roomId : chatRoom.keySet()) {
                room.append(chatRoom.get(roomId) + "\n");
            }
            out.println(room);
        }
    }
    public void createRoom() {
        if (chatRoom.isEmpty()) {
            roomId = 1;
        } else if (chatRoom.size() == roomId) {
            ++roomId;
        }
        String roomName = roomId + "번 방";
        roomList.add(roomName);
        chatRoom.put(roomId, roomList);
        System.out.println(chatRoom.get(roomId));
        out.println(roomName + "이 생성되었습니다.");
    }

    public void joinRoom(String msg) {
        int joinNum = Integer.parseInt(msg.split("\\s")[1]);
        System.out.println(joinNum);
        if (!chatRoom.containsKey(joinNum)) {
            roomListView();
            out.println("리스트를 확인하여 양식에 맞게 방 번호를 올바르게 입력해주세요.");
        } else {

        }
    }

    private void whisperMsg(String msg) {   // 귓속말
        PrintWriter out = chatClients.get(nickname);

        int firstSpaceIndex = msg.indexOf(" ");
        if(firstSpaceIndex == -1)
            return;   // 공백 없으면 돌아가


        int secondSpaceIndex = msg.indexOf(" ", firstSpaceIndex + 1);
        if (secondSpaceIndex == -1)
            return; // 보낼 메시지가 없음 돌아가


        String whisper = msg.substring(firstSpaceIndex + 1, secondSpaceIndex);   // 수신자
        String message = msg.substring(secondSpaceIndex + 1);

        // whisper(수신자)에게 메시지 전송
        PrintWriter pw = chatClients.get(whisper);
        if (pw != null) {
            pw.println(nickname + "님으로부터 온 귓속말 : "+ message);
        }
        else {  // pw == null이라면 아이디 잘못 입력한 거
            out.println("Error : 수신자 " + whisper + "님을 찾을 수 없습니다.");
        }
    }

}
