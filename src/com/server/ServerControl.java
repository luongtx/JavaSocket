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
    private ServerStartFrm startFrm;
    public ServerControl() {
        dao = new ServerDAO();
        dbConn = dao.getConnection();
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
            //to serve multiple clients
            while (true) {
                try {
                    //accept a client connection request
                    socketConn = myServer.accept();
                    System.out.println("server accept request");
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

        public ClientHandler(Socket conn) {
            this.conn = conn;
            try{
                ois = new ObjectInputStream(conn.getInputStream());
                oos = new ObjectOutputStream(conn.getOutputStream());
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
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
    }
    public ArrayList<User> getOnlineUsers(){
        return onlineUsers;
    }
}

