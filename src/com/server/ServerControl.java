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
    public void initUI(){
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
        public void run(){
            String request = "";
            while (true) {
                try {
                    request = (String) ois.readObject();
                    handle(request);
                } catch (Exception ex) {
                    System.out.println("Client closed!");
                    break;
                }
            }
        }
        public void handle(String request){
            try {
                switch (request) {
                    case "LOGIN":
                        User user = (User) ois.readObject();
                        String status = "";
                        if(dao.getUserAccount(user)) status = "OK";
                        else status = "NOTFOUND";
                        oos.writeObject(status);
                        oos.flush();
                        if(status.equals("OK")) {
                            System.out.println("username: " + user.getUsername());
                            if(!checkOnline(user)) onlineUsers.add(user);
                        }
                        break;
                    case "SIGNUP":
                        user = (User) ois.readObject();
//                        System.out.println("username: " + user.getUsername());
                        status = "";
                        if(dao.addUserAccount(user)) status = "OK";
                        else status = "EXISTED";
                        oos.writeObject(status);
                        oos.flush();
                        break;
                    case "GETONLINEUSERS":
                        oos.reset();//remove old objects in stream
                        System.out.println("online: " + onlineUsers.size());
                        oos.writeObject(onlineUsers);
                        break;
                    case "LOGOUT":
                        try{
                            user = (User) ois.readObject();
                            onlineUsers.remove(getUserIndex(user));
                            System.out.println("online: " + onlineUsers.size());
                            //update roomList
                            ArrayList<User> listUser;
                            try{
                                for(Room r: roomList){
                                    if(r.getBoss().getUsername().equals(user.getUsername())) roomList.remove(r);
                                    listUser = r.getUserList();
                                    for(User u: listUser){
                                        if(u.getUsername().equals(user.getUsername())) {
                                            listUser.remove(u);
                                        }
                                    }
                                    r.setUserList(listUser);
                                }
                            }catch(Exception ex){
                                
                            }
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                        break;
                    case "UPDATEROOMS":
                        roomList = (ArrayList<Room>) ois.readObject();
                        System.out.println("number of rooms: "+roomList.size());
                        for(Room room: roomList){
                            System.out.println("[room info]");
                            System.out.println("room name: "+room.getRoomName());
                            System.out.println("room current status: "+room.getStatus());
//                            ArrayList<User> users = room.getUserList();
//                            for(User u: users) System.out.println(u.getUsername());
                        }
                        break;
                    case "GETROOMS":
                        oos.reset();
                        System.out.println("number of rooms: "+roomList.size());
                        oos.writeObject(roomList);
                        break;
                    case "START":
                        System.out.println("start game");
                        processClientGame();
//                        oos.write;
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                
            }
        }
        public void processClientGame() {
            while (running) {
                String sentence = "";
                try {
                    System.out.println("[listening player]");
                    sentence = dis.readUTF();
                    System.out.println("[accept player]");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }

                System.out.println(sentence);
                if (sentence.startsWith("Hello")) {
                    int pos = sentence.indexOf(',');
                    int x = Integer.parseInt(sentence.substring(5, pos));
                    int y = Integer.parseInt(sentence.substring(pos + 1, sentence.length()));

                    sendToClient(protocol.IDPacket(players.size() + 1));
                    try {
                        BroadCastMessage(protocol.NewClientPacket(x, y, 1, players.size() + 1));
                        sendAllClients(dos);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    //add client to client list
                    players.add(new Player(dos, x, y, 1));
                    System.out.println(players.size());
                } else if (sentence.startsWith("Update")) {
                    int pos1 = sentence.indexOf(',');
                    int pos2 = sentence.indexOf('-');
                    int pos3 = sentence.indexOf('|');
                    int x = Integer.parseInt(sentence.substring(6, pos1));
                    int y = Integer.parseInt(sentence.substring(pos1 + 1, pos2));
                    int dir = Integer.parseInt(sentence.substring(pos2 + 1, pos3));
                    int id = Integer.parseInt(sentence.substring(pos3 + 1, sentence.length()));
                    System.out.println("id: "+id);
                    if (players.get(id - 1) != null) {
                        players.get(id - 1).setPosX(x);
                        players.get(id - 1).setPosY(y);
                        players.get(id - 1).setDirection(dir);
                        try {
                            BroadCastMessage(sentence);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                } else if (sentence.startsWith("Shot")) {
                    try {
                        BroadCastMessage(sentence);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (sentence.startsWith("Remove")) {
                    int id = Integer.parseInt(sentence.substring(6));

                    try {
                        BroadCastMessage(sentence);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    players.set(id - 1, null);
                } else if (sentence.startsWith("Exit")) {
                    int id = Integer.parseInt(sentence.substring(4));

                    try {
                        BroadCastMessage(sentence);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (players.get(id - 1) != null) {
                        players.set(id - 1, null);
                    }
                }
            }
        }
        
        private boolean checkOnline(User cUser){
            for(User user: onlineUsers){
                if(user.getUsername().equals(cUser.getUsername())) return true;
            }
            return false;
        }
        private int getUserIndex(User user) {
            int size = onlineUsers.size();
            for (int i = 0; i < size; i++) {
                if (onlineUsers.get(i).getUsername().equals(user.getUsername())) {
                    return i;
                }
            }
            return -1;
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
        public void BroadCastMessage(String mess) throws IOException {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i) != null) {
                    players.get(i).getWriterStream().writeUTF(mess);
                }
            }
        }

        //send to all client current tank state

        public void sendAllClients(DataOutputStream writer) {
            int x, y, dir;
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i) != null) {
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
    }
     
    public ArrayList<User> getOnlineUsers(){
        return onlineUsers;
    }
    public class Player {

        DataOutputStream writer;
        int posX, posY, direction;

        public Player(DataOutputStream writer, int posX, int posY, int direction) {
            this.writer = writer;
            this.posX = posX;
            this.posY = posY;
            this.direction = direction;
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

