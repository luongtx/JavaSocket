/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
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
    public Client(){
        connectServer();
        openUDPSocket();
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
    public int login(User user) {
        try {
            loggedUsers = getOnlineUsers();
            if(getUserByName(user.getUsername())!=null) return -1;
            user.setIpAddress(InetAddress.getLocalHost());
            user.setPort(udpSocket.getLocalPort());
            oos.writeObject("LOGIN");
            oos.writeObject(user);
            String msg = (String) ois.readObject();
            if (msg.equals("OK")) {
                currentUser = user;
                System.out.println("user: "+currentUser.getUsername());
                System.out.println("ip: "+currentUser.getIpAddress());
                System.out.println("port: "+currentUser.getPort());
                return 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    public boolean logout(){
        try{
            oos.writeObject("LOGOUT");
            oos.writeObject(currentUser);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return true;
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
    public void requestBattle(String opponent){
        try {
            //send battle request
            byte[] send_buf = currentUser.getUsername().getBytes();         
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
    public void listenBattleRequest(){
        while(true){
            try {
                //receive request
                byte[] receive_buf = new byte[1024];
                receivePk = new DatagramPacket(receive_buf, receive_buf.length);
                udpSocket.receive(receivePk);
                String msg = new String(receivePk.getData());
                if(msg.contains("OK")){
                    PlayGround battleGround= new PlayGround(this);
                    battleGround.setVisible(true);
                    System.out.println("my battleground");
                }else if(msg.contains("CANCEL")) {
                    JOptionPane.showMessageDialog(null, "request denied!");
                }else{
                    String ans;
                    int input = JOptionPane.showConfirmDialog(null, "Do you want to play with "+ msg, "Confirm battle request", JOptionPane.OK_CANCEL_OPTION);
                    if(input==0) {
                        ans = "OK";
                        PlayGround battleGround= new PlayGround(this);
                        battleGround.setVisible(true);
                        System.out.println("opponent battleground");
                    }
                    else ans = "CANCEL";
                    //reply
                    byte[] send_buf = ans.getBytes();
                    sendPk = new DatagramPacket(send_buf, send_buf.length, receivePk.getSocketAddress());
                    System.out.println("request opponent address:  "+receivePk.getSocketAddress());
                    udpSocket.send(sendPk);
                }
            } catch (Exception ex) {
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
    public User getCurrentUser(){
        return currentUser;
    }
    public InetAddress getPrivateAddress(){
        InetAddress inet = null;
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if(i.isSiteLocalAddress()) return inet = i;
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return inet;
    }
    
}
