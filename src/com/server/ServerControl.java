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
    private ArrayList<User> onlineUsers = new ArrayList<>();
    private ArrayList<Room> roomList = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private ServerStartFrm startFrm;
    private Protocol protocol;
    private boolean running = true;

    public ServerControl() {
        dao = new ServerDAO();
        dbConn = dao.getConnection();
        protocol = new Protocol();
        initUI();
        listenning(serverPort);
    }

    public void initUI() {
        startFrm = new ServerStartFrm(this);
        startFrm.setVisible(true);
    }

    public void listenning(int portNumber) {
        try {
            //create(bind) server socket to listenning client on specified port
            myServer = new ServerSocket(portNumber);
            System.out.println("Server is listenning...");
            //to serve multiple players
            while (true) {
                try {
                    //accept a client connection request
                    socketConn = myServer.accept();
                    System.out.println("server accept client connection");
                    ClientHandler clientHandler = new ClientHandler(socketConn);
                    clientHandler.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    break;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
                    ex.printStackTrace();
                    break;
                }
            }
        }

        public void handle(String request) {
            try {
                switch (request) {
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
                            System.out.println("username: " + user.getUsername());
                            if (!checkOnline(user)) {
                                onlineUsers.add(user);
                            }
                        }
                        break;
                    case "SIGNUP":
                        user = (User) ois.readObject();
//                        System.out.println("username: " + user.getUsername());
                        status = "";
                        if (dao.addUserAccount(user)) {
                            status = "OK";
                        } else {
                            status = "EXISTED";
                        }
                        dos.writeUTF(status);
                        oos.flush();
                        break;
                    case "GETONLINEUSERS":
                        oos.reset();//remove old objects in stream
                        System.out.println("online: " + onlineUsers.size());
                        oos.writeObject(onlineUsers);
                        break;
                    case "LOGOUT":
                        try {
                            user = (User) ois.readObject();
                            onlineUsers.remove(getUserIndex(user.getUsername()));
                            System.out.println("online: " + onlineUsers.size());
                            //update roomList
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
                    case "UPDATEROOMS":
                        roomList = (ArrayList<Room>) ois.readObject();
                        System.out.println("number of rooms: " + roomList.size());
                        for (Room room : roomList) {
                            System.out.println("[room info]");
                            System.out.println("room name: " + room.getRoomName());
                            System.out.println("room current status: " + room.getStatus());
//                            ArrayList<User> users = room.getUserList();
//                            for(User u: users) System.out.println(u.getUsername());
                        }
                        break;
                    case "GETROOMS":
                        oos.reset();
                        System.out.println("number of rooms: " + roomList.size());
                        oos.writeObject(roomList);
                        break;

                    case "REGISTER":
//                        dis = new DataInputStream(conn.getInputStream());
                        String msg = dis.readUTF();
                        String[] data = msg.split(",");
                        String userName = data[0];
                        int x = Integer.parseInt(data[1]);
                        int y = Integer.parseInt(data[2]);
//                        dos = new DataOutputStream(conn.getOutputStream());
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
                    case "UPDATE":
//                        System.out.println("UPDATE");
                        msg = dis.readUTF();
                        System.out.println(msg);
                        data = msg.split(",");
                        x = Integer.parseInt(data[0]);
                        y = Integer.parseInt(data[1]);
                        int dir = Integer.parseInt(data[2]);
                        int id = Integer.parseInt(data[3]);
                        System.out.println("id: " + id);
                        if (players.get(id - 1) != null) {
                            players.get(id - 1).setPosX(x);
                            players.get(id - 1).setPosY(y);
                            players.get(id - 1).setDirection(dir);
                            BroadCastMessage("Update"+msg);
                        }
                        break;
                    case "SHOT":
                        msg = dis.readUTF();
                        System.out.println(msg);
                        id = Integer.parseInt(msg);
                        BroadCastMessage("Shot"+msg);
                        break;
                    case "REMOVE":
                        msg = dis.readUTF();
                        System.out.println(msg);
                        data = msg.split(" ");
                        String playerName = data[0];
                        User user1 = onlineUsers.get(getUserIndex(playerName));
                        int score = user1.getScore();
                        user1.setScore(score + 50);
                        id = Integer.parseInt(data[1]);
                        playerName = players.get(id-1).getPlayerName();
                        User user2 = onlineUsers.get(getUserIndex(playerName));
                        int lose = user2.getLose();
                        user2.setLose(lose+1);
                        BroadCastMessage("Remove"+id);
                        if (players.get(id - 1) != null) {
                            players.set(id - 1, null);
//                            System.out.println("player died: "+id);
                        }
                        int alive = 0;
                        for(Player p: players){
                            if(p!=null) alive ++;
                        }
                        System.out.println("alive: "+alive);
                        if(alive==1) {
                            user1.setWin(user1.getWin()+1);
                            sendToClient("Win");
                            dao.updateResult(onlineUsers);
                            players.clear();
                        }
                        break;
                    case "EXIT":
                        msg = dis.readUTF();
                        id = Integer.parseInt(msg);
                        BroadCastMessage("Exit"+msg);
                        if (players.get(id - 1) != null) {
                            players.set(id-1,null);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }

        public void BroadCastMessage(String mess) throws IOException {
            int n = players.size();
            System.out.println("players size: "+players.size());
            if(n==0) return;
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i) != null) {
                    players.get(i).getWriterStream().writeUTF(mess);
                }
            }
        }

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

        //send to all client current tanks state
        public void sendAllClients(DataOutputStream writer) {
            int n = players.size();
            if(n==0) return;
            int x, y, dir;
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i) != null) {
                    System.out.println("player name: "+players.get(i).getPlayerName());
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

        private boolean checkOnline(User cUser) {
            for (User user : onlineUsers) {
                if (user.getUsername().equals(cUser.getUsername())) {
                    return true;
                }
            }
            return false;
        }

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
