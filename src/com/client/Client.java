/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import com.client.lobby.ClientLoginFrm;
import com.client.lobby.ClientRoomFrm;
import com.client.lobby.Room;
import com.client.playground.ClientPlayGUI;
import com.client.playground.Protocol;
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
    private Protocol protocol;
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
    public Client(){
        currentUser = new User();
        protocol = new Protocol();
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
            try{
                if(getUserByName(user.getUsername())!=null) {
                    JOptionPane.showMessageDialog(roomFrm, "This account currently logged in!");
                    return;
                }
            }catch(Exception ex){
                
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
            currentUser.setLogin(false);
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
            oos.reset();
            roomList.add(room);
            oos.writeObject("UPDATEROOMS");
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
            oos.reset();
            oos.writeObject("UPDATEROOMS");
            oos.writeObject(roomList);
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
    public void sendRoomMsg(String msg, int roomID) throws InterruptedException{
        Room aRoom = roomList.get(roomID);
        ArrayList<User> roomMembers = aRoom.getUserList();
        System.out.println("current room size: "+roomMembers.size());
        for(User user: roomMembers){
            sendMsg(msg, user.getIpAddress(), user.getPort());
            Thread.sleep(500);
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
                        oos.writeObject("START");
                        mainGUI = new ClientPlayGUI(this);
                        mainGUI.setVisible(true);
//                        roomFrm.setVisible(false);
                        roomFrm.dispose();
                        sendMsg("BUSY", receivePk.getAddress(), receivePk.getPort());
                        break;
                    case "SOLO":
                        String ans;
                        String rivalName = data[1].trim();
                        int input = JOptionPane.showConfirmDialog(roomFrm, "Do you want to play with "+ rivalName, "Confirm battle request", JOptionPane.OK_CANCEL_OPTION);
                        if(input==JOptionPane.OK_OPTION) {
                            ans = "REPSOLO OK";
                            oos.writeObject("START");
                            mainGUI = new ClientPlayGUI(this);
                            mainGUI.setVisible(true);
                            roomFrm.dispose();
                            Thread.sleep(1000);
//                            sendMsg("BUSY", receivePk.getAddress(), receivePk.getPort());
                        }else ans = "REPSOLO CANCEL";
                        //reply
                        sendMsg(ans, receivePk.getAddress(), receivePk.getPort());
                        break;
                    case "JOIN":
                        int roomID = Integer.parseInt(data[1].trim());
                        int userID = Integer.parseInt(data[2].trim());
                        System.out.println("Join request");
                        System.out.println("userID " + userID);
                        System.out.println("roomID " + roomID);
//                        System.out.println("current user "+ currentUser.getUsername());
                        getOnlineUsers();
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
                            sendMsg(ans, receivePk.getAddress(), receivePk.getPort());
                            System.out.println("current room status: "+roomList.get(roomID).getStatus());
                        }
                        if (roomList.get(roomID).getStatus().contains("full")) {
                            System.out.println("room full!");
                            sendRoomMsg("START", roomID);
                        }
                        roomFrm.updateTbRoom(roomList);
                        oos.reset();
                        oos.writeObject("UPDATEROOMS");
                        oos.writeObject(roomList);
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
                            oos.writeObject("START");
                            mainGUI = new ClientPlayGUI(this);
                            mainGUI.setVisible(true);
//                            roomFrm.setVisible(false);
                            roomFrm.dispose();
//                            sendMsg("BUSY", receivePk.getAddress(), receivePk.getPort());
                        }else{
                            JOptionPane.showMessageDialog(roomFrm, "request denied!");
                        }
                        break;
                    case "BUSY":
                        JOptionPane.showMessageDialog(roomFrm, "opponent is busy!");
                        break;
                    default:
                        break;
                }
               
            }catch(Exception ex){
                ex.printStackTrace();
            } finally {
                if (udpSocket.isClosed()) {
                    System.out.println("closed udp socket");
//                    sendMsg("BUSY", receivePk.getAddress(), receivePk.getPort());
                    break;
                }
            }
        }
    }
    public void register(int posX, int posY) throws IOException {
        dos = new DataOutputStream(mySocket.getOutputStream());

        dos.writeUTF(protocol.RegisterPacket(posX, posY));

    }
    //send msg to server
    public void sendToServer(String message) {
        if (message.equals("exit")) {
            System.exit(0);
        } else {
            try {
                System.out.println(message);
                dos = new DataOutputStream(mySocket.getOutputStream());
                dos.writeUTF(message);
            } catch (IOException ex) {

            }
        }

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

    public Socket getSocket() {
        return mySocket;
    }
    
}
