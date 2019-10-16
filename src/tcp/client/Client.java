/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author luongtx
 */
public class Client {

    private Socket mySocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private static String ipAddress = "localhost";
    private static int port = 7777;
    private User currentUser;
    public Client(){
        openConnection();
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
    public boolean login(User user) {
        try {
            if (user != null) {
                oos.writeObject("LOGIN");
                oos.writeObject(user);
                String msg = (String) ois.readObject();
                if (msg.equals("OK")) {
                    user.setLogin(true);
                    currentUser = user;
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    public boolean logout(){
        try{
            oos.writeObject("LOGOUT");
            oos.writeObject(currentUser);
            currentUser.setLogin(false);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return true;
    }

    public boolean signUp(User user) {
        try {
            if (user != null) {
                oos.writeObject("SIGNUP");
                oos.writeObject(user);
                String msg = (String) ois.readObject();
                if (msg.equals("OK")) return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    private ArrayList<User> userList;
    public ArrayList<User> getOnlineUsers(){
        try {
            oos.writeObject("GETONLINEUSERS");
            userList = (ArrayList<User>) ois.readObject();
            System.out.println(userList.size());
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return userList;
    }
    public User getCurrentUser(){
        return currentUser;
    }
}
