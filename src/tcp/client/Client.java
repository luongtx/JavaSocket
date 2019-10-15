/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author luongtx
 */
public class Client {

    private Socket mySocket;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;
    private static String ipAddress = "localhost";
    private static int port = 7777;
    private static ClientLoginFrm loginFrm;
    private static ClientSignUpFrm signUpFrm;
    private static RoomFrm roomFrm;
    private static User currentUser;
    public Client(ClientLoginFrm loginView, ClientSignUpFrm signUpView){
        openConnection();
        loginFrm = loginView;
        signUpFrm = signUpView;
    }
    public void openConnection(){
        if(mySocket==null){
            try{
                mySocket = new Socket(ipAddress, port);
                oos = new ObjectOutputStream(mySocket.getOutputStream());
                ois = new ObjectInputStream(mySocket.getInputStream());
            }catch(Exception ex){
                ex.printStackTrace();
            }
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
    public static void login(User user) {
        try {
            if (user != null) {
                oos.writeObject("LOGIN");
                oos.writeObject(user);
                String msg = (String) ois.readObject();
                if (msg.equals("OK")) {
                    JOptionPane.showMessageDialog(null, "login successfully!");
                    user.setLogin(true);
                    currentUser = user;
                    roomFrm = new RoomFrm();
                    roomFrm.setVisible(true);
                    loginFrm.dispose();
                } else if (msg.equals("NOTFOUND")) {
                    JOptionPane.showMessageDialog(null, "no such account!");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    public static void logout(){
        try{
            oos.writeObject("LOGOUT");
            oos.writeObject(currentUser);
            currentUser.setLogin(false);
            loginFrm = new ClientLoginFrm();
            loginFrm.setVisible(true);
            roomFrm.dispose();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void signUp(User user) {
        try {
            if (user != null) {
                oos.writeObject("SIGNUP");
                oos.writeObject(user);
                String msg = (String) ois.readObject();
                if (msg.equals("OK")) {
                    JOptionPane.showMessageDialog(null, "sign up ssuccessfully!");
                } else if (msg.equals("EXISTED")) {
                    JOptionPane.showMessageDialog(null, "account already registered!");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    public static ArrayList<User> userList;
    public static void getOnlineUsers(){
        try {
            oos.writeObject("GETONLINEUSERS");
            userList = (ArrayList<User>) ois.readObject();
            System.out.println(userList.size());
            RoomFrm.lstRoom.removeAll();
            userList.forEach((u) -> {
                RoomFrm.lstRoom.add(u.getUsername());
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static User getCurrentUser(){
        return currentUser;
    }
}
