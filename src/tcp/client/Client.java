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
import javax.swing.JOptionPane;

/**
 *
 * @author luongtx
 */
public class Client {

    private static Socket mySocket;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;
    private static String ipAddress = "localhost";
    private static int port = 5555;
    private final ClientLoginFrm loginFrm;
    private final ClientSignUpFrm signUpFrm;

    public Client(ClientLoginFrm loginView, ClientSignUpFrm signUpView){
        openConnection();
        this.loginFrm = loginView;
        this.signUpFrm = signUpView;
    }
    public static void openConnection(){
        try{
            mySocket = new Socket(ipAddress, port);
            oos = new ObjectOutputStream(mySocket.getOutputStream());
            ois = new ObjectInputStream(mySocket.getInputStream());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public static void closeConnection(){
        try{
            oos.close();
            ois.close();
            mySocket.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public static void login(User user) {
        try {
            if (user != null) {
                oos.writeObject(user);
                String msg = (String) ois.readObject();
                if (msg.equals("OK")) {
                    JOptionPane.showMessageDialog(null, "login successfully!");
                } else if (msg.equals("NOTFOUND")) {
                    JOptionPane.showMessageDialog(null, "no such account!");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void signUp(User user) {
        try {
            if (user != null) {
                oos.writeObject(user);
                String msg = (String) ois.readObject();
                if (msg.equals("OK")) {
                    JOptionPane.showMessageDialog(null, "sign up ssuccessfully!");
                } else if (msg.equals("EXISTED")) {
                    JOptionPane.showMessageDialog(null, "account already exists!");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
