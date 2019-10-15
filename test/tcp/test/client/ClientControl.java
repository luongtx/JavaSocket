/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.test.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class ClientControl {
    private Socket socket;
//    private ObjectInputStream ois;
//    private ObjectOutputStream oos;
    private String ipAddress = "localhost";
    private int port = 7777;
    private ClientLoginFrm loginFrm;
    private ClientSignUpFrm signUpFrm;
    private RoomFrm roomFrm;
    private MainFrm mainFrm;
    private User user;
    public ClientControl(ClientLoginFrm loginView, ClientSignUpFrm signUpView){
        this.loginFrm = loginView;
        this.signUpFrm = signUpView;
        this.roomFrm = new RoomFrm();
        openConnection();
        loginFrm.addLoginListener(new LoginListener());
//        loginFrm.addRenderSignUpFrmListener(new MouseAdapter() { 
//        @Override
//        public void mouseClicked(MouseEvent e) {
//            loginFrm.setVisible(false);
//            signUpFrm.setVisible(true);
//        }});
//        signUpFrm.addSignUpListener(new SignUpListener());
//        signUpFrm.addQuitListner((ActionEvent e) -> {
//            loginFrm.setVisible(true);
//            signUpFrm.setVisible(false);
//        });
//        roomFrm.addRefreshListener(new RefreshListener());
//        roomFrm.addJoinRoomListener(new JoinRoomListener());
//        roomFrm.addViewRoomListener(new ViewRoomListener());
//        roomFrm.addExitListener((ActionEvent e) ->{
////            sendRequest("LOGOUT");
//            user.setLogin(false);
//            loginFrm.setVisible(true);
//            roomFrm.setVisible(false);
//        });
    }
    public void openConnection(){
        try{
            socket = new Socket(ipAddress, port);
            System.out.println("socket "+socket);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public void closeConnection(){
        try{
            socket.close();
        }catch(Exception ex){
            
        }
    }
    private class JoinRoomListener implements ActionListener {

        public JoinRoomListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            sendRequest("JOINROOM");
            String opponent = RoomFrm.lstRoom.getSelectedItem();
            try  {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(opponent);
                String ans = (String) ois.readObject();
                if (ans.equals("YES")) {
                    mainFrm = new MainFrm();
                    mainFrm.setVisible(true);
                    roomFrm.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(null, "request was refused!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
        }
    }

    private class ViewRoomListener implements ActionListener {

        public ViewRoomListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            sendRequest("VIEWROOM");
            mainFrm = new MainFrm();
            mainFrm.setVisible(true);
            roomFrm.setVisible(false);
        }
    }
    private class RefreshListener implements ActionListener {

        public RefreshListener() {
            
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            sendRequest("GETONLINEUSERS");
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ArrayList<User> userList = (ArrayList<User>) ois.readObject();
                for (User u : userList) {
                    RoomFrm.lstRoom.add(u.getUsername());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    

    public class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            user = loginFrm.getLoginUser();
            System.out.println(user);
//            try{
//                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
//                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//                user = loginFrm.getLoginUser();
//                System.out.println(user);
//                if(user!=null){
//                    oos.writeObject(user);
//                    oos.flush();
//                    String msg = (String)ois.readObject();
//                    if(msg.equals("OK")) {
//                        user.setLogin(true);
//                        JOptionPane.showMessageDialog(null, "login successfully!");
//                        roomFrm.setVisible(true);
////                        ArrayList<User> userList = (ArrayList<User>) ois.readObject();
////                        for(User u: userList) System.out.println(u.getUsername());
//                    }
//                    else if(msg.equals("NOTFOUND")) JOptionPane.showMessageDialog(null, "no such account!");
//                }
//            } catch (IOException | ClassNotFoundException ex) {
//                ex.printStackTrace();
//            }
        }
    }

    public class SignUpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
           try{
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                User user = signUpFrm.getSignUpUser();
                if(user!=null) {
                    oos.writeObject(user);
                    oos.flush();
                    String msg = (String)ois.readObject();
                    if(msg.equals("OK")) JOptionPane.showMessageDialog(null, "sign up ssuccessfully!");
                    else if(msg.equals("EXISTED")) JOptionPane.showMessageDialog(null, "account already exists!");
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
}
