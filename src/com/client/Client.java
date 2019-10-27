/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import com.client.lobby.ClientLoginFrm;
import com.client.lobby.ClientRoomFrm;
import com.client.playground.ClientPlayGUI;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author luongtx
 */
public class Client {

    private Socket mySocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataInputStream dis;
    private DataOutputStream dos;
    private static String serverAddress = "localhost";
    private static int serverPort = 7777;
    private DatagramSocket udpSocket;
    private DatagramPacket sendPk, receivePk;
    private User currentUser;
    private static ArrayList<User> loggedUsers;
    public static ArrayList<Room> roomList;
    private ClientLoginFrm loginFrm;
    private ClientRoomFrm roomFrm;
    private ClientPlayGUI mainGUI;
//    public static ClientTestFrm testFrm;
    public Client(){
        currentUser = new User();
        connectServer();
        openUDPSocket();
        initUI();
        listenRequest();
    }
    public void initUI(){
        loginFrm = new ClientLoginFrm(this);
        loginFrm.setVisible(true);
    }
    public void connectServer(){
        if(mySocket==null){
            try{
                mySocket = new Socket(serverAddress, serverPort);
                oos = new ObjectOutputStream(mySocket.getOutputStream());
                ois = new ObjectInputStream(mySocket.getInputStream());
                dos = new DataOutputStream(mySocket.getOutputStream());
                dis = new DataInputStream(mySocket.getInputStream());
//                System.out.println("local socket: "+mySocket.getLocalSocketAddress());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void openUDPSocket(){
        try{
            udpSocket = new DatagramSocket();
//            replyBattleRequest();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void closeConnection(){
        if(mySocket!=null){
            try{
                oos.close();
                ois.close();
                dos.close();
                dis.close();
                mySocket.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public User getUserByName(String userName){
        for(User user: loggedUsers){
            if(user.getUsername().equals(userName)){
                return user;
            }
        }
        return null;
    }
    public void login(User user) {
        try {
            loggedUsers = getOnlineUsers();
            System.out.println("loggedUser size: "+loggedUsers.size());
            try{
                if(getUserByName(user.getUsername())!=null) {
                    JOptionPane.showMessageDialog(roomFrm, "This account currently logged in!");
                    return;
                }
            }catch(Exception ex){
                
            }
            if(getUserByName(user.getUsername())==null){
                user.setIpAddress(InetAddress.getLocalHost());
                user.setPort(udpSocket.getLocalPort());
            }
            dos.writeUTF("LOGIN");
            oos.writeObject(user);
            String msg = dis.readUTF();
            System.out.println("login stt: "+msg);
            if (msg.equals("OK")) {
                currentUser = user;
                System.out.println("user: "+currentUser.getUsername());
                System.out.println("ip: "+currentUser.getIpAddress());
                System.out.println("port: "+currentUser.getPort());
                roomFrm = new ClientRoomFrm(this);
                roomFrm.setVisible(true);
//                loginFrm.setVisible(false);
                loginFrm.dispose();
            }else{
                JOptionPane.showMessageDialog(loginFrm, "Wrong username or password!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void logout(){
        try{
            roomFrm.dispose();
            currentUser.setLogin(false);
            dos.writeUTF("LOGOUT");
            oos.writeObject(currentUser);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public boolean signUp(User user) {
        try {
            dos.writeUTF("SIGNUP");
            oos.writeObject(user);
            String msg = (String) dis.readUTF();
            if (msg.equals("OK")) return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
   
    public void requestSolo(int enemyId){
        try {
            String req = "SOLO " + currentUser.getUsername();
            byte[] send_buf = req.getBytes();
            User enemy = loggedUsers.get(enemyId);
            if(currentUser.getUsername().equals(enemy.getUsername())){
                JOptionPane.showMessageDialog(null, "lol!");
                return;
            }
            System.out.println("enemy ip: "+enemy.getIpAddress());
            System.out.println("enemy port: "+enemy.getPort());
            sendPk = new DatagramPacket(send_buf, send_buf.length, enemy.getIpAddress(), enemy.getPort());
            udpSocket.send(sendPk);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean isJoined(int roomID, User user){
        Room aRoom = roomList.get(roomID);
        ArrayList<User> aList = aRoom.getUserList();
        for(User u: aList){
            if(u.getUsername().equals(user.getUsername())) return true;
        }
        return false;
    }
    public void requestJoinRoom(int roomID){
        try{
            if(isJoined(roomID,currentUser)) {
                JOptionPane.showMessageDialog(roomFrm, "You're currently in this room!");
                return;
            }
            int userID = loggedUsers.indexOf(getUserByName(currentUser.getUsername()));
//            System.out.println("User id "+userID);
            String msg = "JOIN " + roomID + " " + userID;
            byte[] send_buf = msg.getBytes();
            User boss = roomList.get(roomID).getBoss();
            sendPk = new DatagramPacket(send_buf, send_buf.length, boss.getIpAddress(), boss.getPort());
            udpSocket.send(sendPk);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public void createRoom(Room room){
        try{
            roomList.add(room);
            System.out.println("roomList size: "+roomList.size());
            dos.writeUTF("UPDATEROOMS");
//            oos.reset();
            oos.writeObject(roomList);
//            oos.flush();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void deleteRoom(int roomID){
        try {
            User roomBoss = roomList.get(roomID).getBoss();
            if(!roomBoss.getUsername().equals(currentUser.getUsername())){
                JOptionPane.showMessageDialog(roomFrm, "You don't have permission");
                return;
            }
            roomList.remove(roomID);
//            oos.reset();
            dos.writeUTF("UPDATEROOMS");
            oos.writeObject(roomList);
//            oos.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public boolean addUserToRoom(int userID, int roomID){
        Room aRoom = roomList.get(roomID);
        if(aRoom.getStatus().contains("full")){
            JOptionPane.showMessageDialog(roomFrm, "room full!");
            return false;
        }
        ArrayList<User> aList = aRoom.getUserList();
        User aUser = loggedUsers.get(userID);
        aList.add(aUser);
        aRoom.setUserList(aList);
        roomList.set(roomID, aRoom);
        return true;
    }
    public void sendToPeer(String msg, InetAddress address, int port){
        try {
            byte[] send_buf = msg.getBytes();
            sendPk = new DatagramPacket(send_buf, send_buf.length,address,port);
            udpSocket.send(sendPk);
            System.out.println("send msg: "+msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //send msg to all room member
    public void sendRoomMsg(String msg, int roomID) throws InterruptedException{
        Room aRoom = roomList.get(roomID);
        ArrayList<User> roomMembers = aRoom.getUserList();
        System.out.println("current room size: "+roomMembers.size());
        for(User user: roomMembers){
            sendToPeer(msg, user.getIpAddress(), user.getPort());
            Thread.sleep(500);
        }
    }
    
    public void listenRequest(){
        while(true){
            try{
                byte[] receive_buf = new byte[1024];
                receivePk = new DatagramPacket(receive_buf, receive_buf.length);
                System.out.println("[Listening battle request]");
                udpSocket.receive(receivePk);
                System.out.println("[Accept request]");
                String receiveMsg = new String(receivePk.getData());
                String [] data = receiveMsg.split(" ");
                String req = data[0].trim();
                switch(req){
                    case "START":
                        System.out.println("trigger start");
//                        oos.writeObject("START");
                        mainGUI = new ClientPlayGUI(this);
                        mainGUI.setVisible(true);
                        roomFrm.setVisible(false);
//                        sendToPeer("BUSY", receivePk.getAddress(), receivePk.getPort());
                        break;
                    case "SOLO":
                        String ans;
                        String rivalName = data[1].trim();
                        int input = JOptionPane.showConfirmDialog(roomFrm, "Do you want to play with "+ rivalName, "Confirm battle request", JOptionPane.OK_CANCEL_OPTION);
                        if(input==JOptionPane.OK_OPTION) {
                            ans = "REPSOLO OK";
//                            oos.writeObject("START");
                            mainGUI = new ClientPlayGUI(this);
                            mainGUI.setVisible(true);
                            roomFrm.setVisible(false);
                            Thread.sleep(500);
                        }else ans = "REPSOLO CANCEL";
                        //reply
                        sendToPeer(ans, receivePk.getAddress(), receivePk.getPort());
//                        udpSocket.close();
//                        System.out.println("stop listening battle request");
//                        sendToPeer("BUSY", receivePk.getAddress(), receivePk.getPort());
                        break;
                    case "JOIN":
                        int roomID = Integer.parseInt(data[1].trim());
                        int userID = Integer.parseInt(data[2].trim());
                        System.out.println("Join request");
                        System.out.println("userID " + userID);
                        System.out.println("roomID " + roomID);
                        System.out.println("current user "+ currentUser.getUsername());
                        getOnlineUsers();
                        System.out.println("opponent: "+loggedUsers.get(userID).getUsername());
                        if(loggedUsers.get(userID).getUsername().equals(currentUser.getUsername())){
                            addUserToRoom(userID, roomID);
                            System.out.println("boss joined room");
                            System.out.println("current room status: "+roomList.get(roomID).getStatus());
                        }else{
                            String roomName = roomList.get(roomID).getRoomName();
                            String userName = loggedUsers.get(userID).getUsername();
                            input = JOptionPane.showConfirmDialog(roomFrm, "Do you want "+userName+" to join room "+roomName,"Confirm join request",JOptionPane.OK_CANCEL_OPTION);
                            if(input==JOptionPane.OK_OPTION && addUserToRoom(userID, roomID)){
                                ans = "REPJOIN OK";
                            }else ans = "REPJOIN CANCEL";
                            sendToPeer(ans, receivePk.getAddress(), receivePk.getPort());
                            System.out.println("current room status: "+roomList.get(roomID).getStatus());
                        }
                        if (roomList.get(roomID).getStatus().contains("full")) {
                            System.out.println("room full!");
                            sendRoomMsg("START", roomID);
                        }
                        roomFrm.updateTbRoom(roomList);
                        dos.writeUTF("UPDATEROOMS");
                        oos.reset();
                        oos.writeObject(roomList);
//                        oos.flush();
                        break;
                    case "REPJOIN":
                        String msg = data[1].trim();
                        if(msg.contains("OK")){
                            JOptionPane.showMessageDialog(roomFrm, "wait for battle!");
                            roomFrm.updateTbRoom(getRooms());
                        }
                        else
                            JOptionPane.showMessageDialog(roomFrm, "request denied!");
                        break;
                    case "REPSOLO":
                        msg = data[1].trim();
                        if(msg.contains("OK")){
//                            oos.writeObject("START");
                            mainGUI = new ClientPlayGUI(this);
                            mainGUI.setVisible(true);
                            roomFrm.setVisible(false);
//                            udpSocket.close();
//                            System.out.println("stop listening battle request");
//                            sendToPeer("BUSY", receivePk.getAddress(), receivePk.getPort());
                        }else{
                            JOptionPane.showMessageDialog(roomFrm, "request denied!");
                        }
                        break;
                    default:
                        break;
                }
               
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void register(int posX, int posY) throws IOException {
//        mySocket = new Socket(serverAddress, serverPort);
        dos.writeUTF("REGISTER");
        dos.writeUTF(currentUser.getUsername()+","+posX+","+posY);
    }
    //send msg to server
    public void sendToServer(String title, String data) {
        try {
            dos.writeUTF(title);
            if (title.equals("REMOVE")) {
                data = currentUser.getUsername() + " " + data;
            }
            dos.writeUTF(data);
            System.out.println(title + data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    public void returnLobby(){
        mainGUI.dispose();
        roomFrm.setVisible(true);
    }
    public ArrayList<User> getOnlineUsers(){
        try {
            dos.writeUTF("GETONLINEUSERS");
            loggedUsers = (ArrayList<User>) ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return loggedUsers;
    }
    public ArrayList<Room> getRooms(){
        try{
            dos.writeUTF("GETROOMS");
            roomList = (ArrayList<Room>) ois.readObject();
        }catch(IOException | ClassNotFoundException ex){
            ex.printStackTrace();
        }catch(NullPointerException ex){
            System.out.println("0 room");
        }
        return roomList;
    }
    public User getCurrentUser(){
        return currentUser;
    }
    
    public ArrayList<User> getLeaderBoard(){
        ArrayList<User> userList = null;
        try {
            dos.writeUTF("GETALLUSERS");
            userList = (ArrayList<User>) ois.readObject();
            Collections.sort(userList, new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    return o2.getScore() - o1.getScore();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return userList;
    }

    public Socket getSocket() {
        return mySocket;
    }
    public DataInputStream getDataInputStream(){
        return dis;
    }
    public DataOutputStream getDataOutputStream(){
        return dos;
    }
    
}
