/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.server;

import com.client.Room;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import com.client.User;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author luongtx
 */
public final class ServerControl {

    private ServerSocket myServer;
    private Socket socketConn;
    private Connection dbConn;
    private final int serverPort = 7777;
    private ServerDAO dao;
    private ArrayList<User> userList = new ArrayList<>();//danh sách tất cả người dùng
    private ArrayList<User> onlineUsers = new ArrayList<>();//danh sách người chơi online(đã đăng nhập)
    private ArrayList<Room> roomList = new ArrayList<>();//danh sách các phòng
    private ArrayList<Player> players = new ArrayList<>();//danh sách người chơi
    private ServerStartFrm startFrm;
    private Protocol protocol;
    public boolean running = true;

    public ServerControl() {
        dao = new ServerDAO();
        dbConn = dao.getConnection();
        protocol = new Protocol();
        initUI();
        listenning(serverPort);
    }
    //khởi tạo giao diện 
    public void initUI() {
        startFrm = new ServerStartFrm(this);
        startFrm.setVisible(true);
    }
    //dừng server
    public void stopServer(){
        try{
            dbConn.close();
            myServer.close();
            running = false;
            System.out.println("Server is stopped");
            System.exit(0);
        }catch(Exception ex){
//            ex.printStackTrace();
        }
    }
    //lắng nghe clients
    public void listenning(int portNumber) {
        try {
            myServer = new ServerSocket(portNumber);
            System.out.println("Server is listenning...");
            while (running) {
                try {
                    //chập nhận kết nối từ clients
                    socketConn = myServer.accept();
                    System.out.println("server accept client connection");
                    ClientHandler clientHandler = new ClientHandler(socketConn);
                    clientHandler.start();
                } catch (Exception ex) {
//                    ex.printStackTrace();
                    break;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //Xử lý request từ clients
    class ClientHandler extends Thread {

        Socket conn;
        ObjectInputStream ois;
        ObjectOutputStream oos;
        DataInputStream dis;
        DataOutputStream dos;
        
        public ClientHandler(Socket conn) {
            this.conn = conn;
            try{
                ois = new ObjectInputStream(conn.getInputStream());
                oos = new ObjectOutputStream(conn.getOutputStream());
                dis = new DataInputStream(conn.getInputStream());
                dos = new DataOutputStream(conn.getOutputStream());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            String request = "";
            while (true) {
                try {
                    request = dis.readUTF();
                    handle(request);
                } catch (Exception ex) {
                    System.out.println("Client closed!");
//                    ex.printStackTrace();
                    break;
                }
            }
        }

        public void handle(String request) {
            try {
                //xử lý các yêu cầu
                switch (request) {
                    //yêu cầu đăng nhập
                    case "LOGIN":
                        User user = (User) ois.readObject();
                        String status = "";
                        user = dao.getUserInfor(user);
                        if (user!=null) {
                            status = "OK";
                        } else {
                            status = "NOTFOUND";
                        }
                        dos.writeUTF(status);
                        if (status.equals("OK")) {
//                            System.out.println("username: " + user.getUsername());
                            if (!checkOnline(user)) {
                                onlineUsers.add(user);
                            }
                        }
                        break;
                    //yêu cầu đăng ký tài khoản
                    case "SIGNUP":
                        user = (User) ois.readObject();
                        status = "";
                        if (dao.addUserAccount(user)) {
                            status = "OK";
                        } else {
                            status = "EXISTED";
                        }
                        dos.writeUTF(status);
                        oos.flush();
                        break;
                    //yêu cầu lấy thông tin người dùng đăng nhập
                    case "GETONLINEUSERS":
                        oos.reset();
//                        System.out.println("online: " + onlineUsers.size());
                        oos.writeObject(onlineUsers);
                        break;
                    //yêu cầu đăng xuất    
                    case "LOGOUT":
                        try {
                            user = (User) ois.readObject();
                            onlineUsers.remove(getUserIndex(user.getUsername()));
                            System.out.println("online: " + onlineUsers.size());
                            //cập nhật lại danh sách các phòng
                            ArrayList<User> listUser;
                            try {
                                for (Room r : roomList) {
                                    if (r.getBoss().getUsername().equals(user.getUsername())) {
                                        roomList.remove(r);
                                    }
                                    listUser = r.getUserList();
                                    for (User u : listUser) {
                                        if (u.getUsername().equals(user.getUsername())) {
                                            listUser.remove(u);
                                        }
                                    }
                                    r.setUserList(listUser);
                                }
                            } catch (Exception ex) {

                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    //yêu cầu cập nhật danh sách phòng    
                    case "UPDATEROOMS":
                        roomList = (ArrayList<Room>) ois.readObject();
//                        System.out.println("number of rooms: " + roomList.size());
                        break;
                    //yêu cầu lấy danh sách phòng
                    case "GETROOMS":
                        oos.reset();
                        oos.writeObject(roomList);
                        break;
                    //yêu cầu gửi vị trí tank
                    case "REGISTER":
                        String msg = dis.readUTF();
                        String[] data = msg.split(",");
                        String userName = data[0];
                        int x = Integer.parseInt(data[1]);
                        int y = Integer.parseInt(data[2]);
                        sendToClient(protocol.IDPacket(players.size() + 1));
                        try {
                            BroadCastMessage(protocol.NewClientPacket(x, y, 1, players.size() + 1));
                            sendAllClients(dos);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        players.add(new Player(userName, dos, x, y, 1));
                        System.out.println(players.size());
                        break;
                    //yêu cầu di chuyển tank    
                    case "MOVE":
                        msg = dis.readUTF();
                        System.out.println(msg);
                        data = msg.split(",");
                        x = Integer.parseInt(data[0]);
                        y = Integer.parseInt(data[1]);
                        int dir = Integer.parseInt(data[2]);
                        int id = Integer.parseInt(data[3]);
//                        System.out.println("id: " + id);
                        if (players.get(id - 1) != null) {
                            players.get(id - 1).setPosX(x);
                            players.get(id - 1).setPosY(y);
                            players.get(id - 1).setDirection(dir);
                            BroadCastMessage("Update"+msg);
                        }
                        break;
                    //yêu cầu shot (nhả bomb)
                    case "SHOT":
                        msg = dis.readUTF();
                        System.out.println(msg);
                        id = Integer.parseInt(msg);
                        BroadCastMessage("Shot"+msg);
                        break;
                    //yêu cầu xóa tank
                    case "REMOVE":
                        msg = dis.readUTF();
                        System.out.println(msg);
                        data = msg.split(" ");
                        //Cập nhật điểm cho player 1 (người nhả bomb)
                        String playerName = data[0];
                        User user1 = onlineUsers.get(getUserIndex(playerName));
                        int score = user1.getScore();
                        user1.setScore(score + 50);
                        //cập nhật điểm cho player 2 (người trúng bomb)
                        id = Integer.parseInt(data[1]);
                        playerName = players.get(id-1).getPlayerName();
                        User user2 = onlineUsers.get(getUserIndex(playerName));
                        int lose = user2.getLose();
                        user2.setLose(lose+1);
                        //thông báo cho tất cả người chơi
                        BroadCastMessage("Remove"+id);
                        if (players.get(id - 1) != null) {
                            players.set(id - 1, null);
//                            System.out.println("player died: "+id);
                        }
                        //kiểm tra xem player 1 có phải là người chơi cuối cùng
                        int alive = 0;
                        for(Player p: players){
                            if(p!=null) alive ++;
                        }
                        System.out.println("alive: "+alive);
                        if(alive==1) {
                            user1.setWin(user1.getWin()+1);
                            sendToClient("Win");
                            dao.updateResult(onlineUsers);
                            players = new ArrayList<>();
                        }
                        break;
                    //yêu cầu thoát game
                    case "EXIT":
                        msg = dis.readUTF();
                        id = Integer.parseInt(msg);
                        BroadCastMessage("Exit" + msg);
                        try {
                            if (players.get(id - 1) != null) {
                                players.set(id - 1, null);
                            }
                        } catch (Exception ex) {

                        }
                        if (isBattleEnd()) {
                            players = new ArrayList<>();
                        }
                        break;
                    //lấy tất cả danh sách users
                    case "GETALLUSERS":
//                        System.out.println("get all users");
                        userList = dao.getAllUsers();
//                        oos.reset();
                        oos.writeObject(userList);
                        break;   
                       // gui tin nhan cho tat ca client
                    default:
                        BroadCastMessage(request);
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }
        //kiểm tra xem trận đấu kết thúc chưa
        public boolean isBattleEnd(){
            for(Player p: players){
                if(p!=null) return false;
            }
            return true;
        }
        //Gửi một thông điệp cho tất cả người chơi
        public void BroadCastMessage(String mess) throws IOException {
            int n = players.size();
//            System.out.println("players size: "+players.size());
            if(n==0) return;
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i) != null) {
                    players.get(i).getWriterStream().writeUTF(mess);
                }
            }
        }
        //Gửi thông điệp cho 1 người chơi
        public void sendToClient(String message) {
            if (message.equals("exit")) {
                System.exit(0);
            } else {
                try {
                    dos.writeUTF(message);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        //gửi cho người chơi thông tin vị trí tank của tất cả các người chơi
        public void sendAllClients(DataOutputStream writer) {
            int n = players.size();
            if(n==0) return;
            int x, y, dir;
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i) != null) {
//                    System.out.println("player name: "+players.get(i).getPlayerName());
                    x = players.get(i).getX();
                    y = players.get(i).getY();
                    dir = players.get(i).getDir();
                    try {
                        writer.writeUTF(protocol.NewClientPacket(x, y, dir, i + 1));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        //kiểm tra người dùng đã đăng nhập chưa
        private boolean checkOnline(User cUser) {
            for (User user : onlineUsers) {
                if (user.getUsername().equals(cUser.getUsername())) {
                    return true;
                }
            }
            return false;
        }
        //trả về id của người dùng
        private int getUserIndex(String username) {
            int size = onlineUsers.size();
            for (int i = 0; i < size; i++) {
                if (onlineUsers.get(i).getUsername().equals(username)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public ArrayList<User> getOnlineUsers() {
        return onlineUsers;
    }
    //Thông tin về người chơi
    class Player {
        String playerName;
        DataOutputStream writer;
        int posX, posY, direction;
        int score = 0;
        public Player(String playerName, DataOutputStream writer, int posX, int posY, int direction) {
            this.playerName = playerName;
            this.writer = writer;
            this.posX = posX;
            this.posY = posY;
            this.direction = direction;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
        
        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public DataOutputStream getWriter() {
            return writer;
        }

        public void setWriter(DataOutputStream writer) {
            this.writer = writer;
        }
        
        public void setPosX(int x) {
            posX = x;
        }

        public void setPosY(int y) {
            posY = y;
        }

        public void setDirection(int dir) {
            direction = dir;
        }

        public DataOutputStream getWriterStream() {
            return writer;
        }

        public int getX() {
            return posX;
        }

        public int getY() {
            return posY;
        }

        public int getDir() {
            return direction;
        }
    }
}
