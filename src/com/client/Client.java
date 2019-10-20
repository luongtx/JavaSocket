/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
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
    private static String serverAddress = "localhost";
    private static int serverPort = 7777;
    private DatagramSocket udpSocket;
    private DatagramPacket sendPk, receivePk;
    private User currentUser;
    private static ArrayList<User> loggedUsers;
    public static ArrayList<Room> roomList;
    private ClientLoginFrm loginFrm;
    private ClientRoomFrm roomFrm;
    private BattleGround battleGround;
    public Client(){
        currentUser = new User();
        connectServer();
        openUDPSocket();
        
//        listenRequest();
    }
    public void initUI(){
        loginFrm = new ClientLoginFrm(this);
        loginFrm.setVisible(true);
        listenRequest();
    }
    public void connectServer(){
        if(mySocket==null){
            try{
                mySocket = new Socket(serverAddress, serverPort);
//                System.out.println("local socket: "+mySocket.getLocalSocketAddress());
                oos = new ObjectOutputStream(mySocket.getOutputStream());
                ois = new ObjectInputStream(mySocket.getInputStream());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void openUDPSocket(){
        try{
            udpSocket = new DatagramSocket();
            currentUser.setIpAddress(InetAddress.getLocalHost());
            currentUser.setPort(udpSocket.getLocalPort());
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
                mySocket.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void login(User user) {
        try {
            loggedUsers = getOnlineUsers();
            if(getUserByName(user.getUsername())!=null) {
                JOptionPane.showMessageDialog(roomFrm, "This account have already logged in!");
                return;
            }
            user.setIpAddress(currentUser.getIpAddress());
            user.setPort(currentUser.getPort());
            oos.writeObject("LOGIN");
            oos.writeObject(user);
            String msg = (String) ois.readObject();
            if (msg.equals("OK")) {
                currentUser = user;
                System.out.println("user: "+currentUser.getUsername());
                System.out.println("ip: "+currentUser.getIpAddress());
                System.out.println("port: "+currentUser.getPort());
//                JOptionPane.showMessageDialog(loginFrm, "Login successfully!");
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
            oos.writeObject("LOGOUT");
            oos.writeObject(currentUser);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public boolean signUp(User user) {
        try {
            oos.writeObject("SIGNUP");
            oos.writeObject(user);
            String msg = (String) ois.readObject();
            if (msg.equals("OK")) return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    public void requestSolo(String opponent){
        try {
            //send battle request
            String req = "SOLO " + currentUser.getUsername(); 
            byte[] send_buf = req.getBytes();
            User user = getUserByName(opponent);
            if(currentUser.getUsername().equals(opponent)) {
                JOptionPane.showMessageDialog(null, "lol!");
                return;
            }
            System.out.println("opponent ip: "+user.getIpAddress());
            System.out.println("opponent port: "+user.getPort());
            sendPk = new DatagramPacket(send_buf, send_buf.length, user.getIpAddress(), user.getPort());
            udpSocket.send(sendPk);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void requestJoinRoom(String roomName){
        try{
            int roomIdx = getRoomIndex(roomName);
            Room aRoom = roomList.get(roomIdx);
            ArrayList<User> aList = aRoom.getUserList();
            User aUser = getUserByName(currentUser.getUsername());
            if(aList.contains(aUser)) {
                JOptionPane.showMessageDialog(roomFrm, "You're currently in this room!");
                return;
            }
            String msg = "JOIN " + roomName + " " + currentUser.getUsername();
            System.out.println("roomName: "+roomName);
            byte[] send_buf = msg.getBytes();
            User boss = roomList.get(roomIdx).getBoss();
            System.out.println("boss name: "+boss.getUsername());
            System.out.println("boss port: "+boss.getPort());
            sendPk = new DatagramPacket(send_buf, send_buf.length, boss.getIpAddress(), boss.getPort());
            udpSocket.send(sendPk);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public void createRoom(Room room){
        try{
            oos.reset();
            roomList.add(room);
            oos.writeObject("UPDATEROOMS");
            oos.writeObject(roomList);
//            oos.flush();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void deleteRoom(String roomName){
        try{
            User roomBoss = roomList.get(getRoomIndex(roomName)).getBoss();
            System.out.println("boss: "+roomBoss.getUsername());
            System.out.println("current user: "+currentUser.getUsername());
            if(!roomBoss.getUsername().equals(currentUser.getUsername())){
                JOptionPane.showMessageDialog(roomFrm, "You don't have permission");
                return;
            }
            roomList.remove(getRoomIndex(roomName));
            oos.reset();
            oos.writeObject("UPDATEROOMS");
            oos.writeObject(roomList);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public int getRoomIndex(String roomName){
        for(Room r: roomList){
            if(r.getRoomName().equals(roomName)) return roomList.indexOf(r);
        }
        return -1;
    }
    public boolean addUserToRoom(String userName, String roomName){
        int roomIdx = getRoomIndex(roomName);
        Room aRoom = roomList.get(roomIdx);
        if(aRoom.getStatus().contains("full")){
            JOptionPane.showMessageDialog(roomFrm, "room full!");
            return false;
        }
        ArrayList<User> aList = aRoom.getUserList();
        User aUser = getUserByName(userName);
        aList.add(aUser);
        aRoom.setUserList(aList);
        roomList.set(getRoomIndex(roomName), aRoom);
        return true;
//        System.out.println("server: "+roomList.get(roomIdx).getStatus());
    }
    public void sendMsg(String msg, InetAddress address, int port){
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
    public void sendRoomMsg(String msg, String roomName) throws InterruptedException{
        Room aRoom = roomList.get(getRoomIndex(roomName));
        ArrayList<User> roomMembers = aRoom.getUserList();
        System.out.println("current room size: "+roomMembers.size());
        for(User user: roomMembers){
//            System.out.println(user.getUsername());
//            System.out.println(user.getIpAddress()+ "/"+ user.getPort());
            sendMsg(msg, user.getIpAddress(), user.getPort());
            Thread.sleep(1000);
        }
    }
    
    public void listenRequest(){
        while(true){
            try{
                byte[] receive_buf = new byte[1024];
                receivePk = new DatagramPacket(receive_buf, receive_buf.length);
                System.out.println("[Listen request from other clients]");
                udpSocket.receive(receivePk);
                System.out.println("[Accept request]");
                String receiveMsg = new String(receivePk.getData());
                String [] data = receiveMsg.split(" ");
                String req = data[0].trim();
                switch(req){
                    case "START":
                        System.out.println("trigger start");
                        battleGround = new BattleGround(this);
                        battleGround.setVisible(true);
//                        roomFrm.setVisible(false);
                        roomFrm.dispose();
                        break;
                    case "SOLO":
                        String ans;
                        int input = JOptionPane.showConfirmDialog(roomFrm, "Do you want to play with "+ data[1], "Confirm battle request", JOptionPane.OK_CANCEL_OPTION);
                        if(input==JOptionPane.OK_OPTION) {
                            ans = "REPSOLO OK";
                            battleGround= new BattleGround(this);
                            battleGround.setVisible(true);
//                            roomFrm.setVisible(false);
                            roomFrm.dispose();
                            System.out.println("opponent battleground");
                        }else ans = "REPSOLO CANCEL";
                        //reply
                        sendMsg(ans, receivePk.getAddress(), receivePk.getPort());
                        break;
                    case "JOIN":
                        String roomName = data[1].trim();
                        String userName = data[2].trim();
                        System.out.println("Join request");
                        System.out.println("userName " + userName);
                        System.out.println("roomName " + roomName);
//                        System.out.println("current user "+ currentUser.getUsername());
                        if(userName.equals(currentUser.getUsername())){
                            addUserToRoom(userName, roomName);
                            System.out.println("Join your room");
                            System.out.println("current room status: "+roomList.get(getRoomIndex(roomName)).getStatus());
                        }else{
                            input = JOptionPane.showConfirmDialog(roomFrm, "Do you want "+userName+" to join room "+roomName,"Confirm join request",JOptionPane.OK_CANCEL_OPTION);
                            if(input==JOptionPane.OK_OPTION && addUserToRoom(userName, roomName)){
                                ans = "REPJOIN OK";
                            }else ans = "REPJOIN CANCEL";
                            sendMsg(ans, receivePk.getAddress(), receivePk.getPort());
                            System.out.println("current room status: "+roomList.get(getRoomIndex(roomName)).getStatus());
                        }
                        if (roomList.get(getRoomIndex(roomName)).getStatus().contains("full")) {
                            System.out.println("room full!");
                            sendRoomMsg("START", roomName);
                        }
                        roomFrm.updateTbRoom(roomList);
                        oos.reset();
                        oos.writeObject("UPDATEROOMS");
                        oos.writeObject(roomList);
                        
                        break;
                    case "REPJOIN":
                        String msg = data[1];
                        if(msg.contains("OK")){
                            JOptionPane.showMessageDialog(roomFrm, "wait for battle!");
                            roomFrm.updateTbRoom(getRooms());
                        }
                        else
                            JOptionPane.showMessageDialog(roomFrm, "request denied!");
                        break;
                    case "REPSOLO":
                        msg = data[1];
                        if(msg.contains("OK")){
                            battleGround = new BattleGround(this);
                            battleGround.setVisible(true);
//                            roomFrm.setVisible(false);
                            roomFrm.dispose();
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
    public User getUserByName(String username){
        User user = null;
        for(User u: loggedUsers){
            if(u.getUsername().equals(username)){
                user = u;
                break;
            }
        }
        return user;
    }
    public ArrayList<User> getOnlineUsers(){
        try {
            oos.writeObject("GETONLINEUSERS");
            loggedUsers = (ArrayList<User>) ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return loggedUsers;
    }
    public ArrayList<Room> getRooms(){
        try{
            oos.reset();
            oos.writeObject("GETROOMS");
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
//    public InetAddress getPrivateAddress(){
//        InetAddress inet = null;
//        try {
//            Enumeration e = NetworkInterface.getNetworkInterfaces();
//            while(e.hasMoreElements())
//            {
//                NetworkInterface n = (NetworkInterface) e.nextElement();
//                Enumeration ee = n.getInetAddresses();
//                while (ee.hasMoreElements())
//                {
//                    InetAddress i = (InetAddress) ee.nextElement();
//                    if(i.isSiteLocalAddress()) return inet = i;
//                }
//            }
//        } catch (SocketException ex) {
//            ex.printStackTrace();
//        }
//        return inet;
//    }
    
}
