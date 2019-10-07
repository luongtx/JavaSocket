/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JOptionPane;

/**
 *
 * @author luongtx
 */
public class ClientControl {
    private Socket mySocket;
    private final String ipAddress = "localhost";
    private final int port = 5555;
    private final ClientLoginFrm loginFrm;
    private final ClientSignUpFrm signUpFrm;
    public ClientControl(ClientLoginFrm loginView, ClientSignUpFrm signUpView){
        this.loginFrm = loginView;
        this.signUpFrm = signUpView;
        loginFrm.addLoginListener(new LoginListener());
        loginFrm.addRenderSignUpFrmListener(new MouseAdapter() { 
        @Override
        public void mouseClicked(MouseEvent e) {
            loginFrm.setVisible(false);
            signUpFrm.setVisible(true);
        }});
        signUpFrm.addSignUpListener(new SignUpListener());
        signUpFrm.addQuitListner((ActionEvent e) -> {
            loginFrm.setVisible(true);
            signUpFrm.setVisible(false);
        });
    }

    public class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try{
                mySocket = new Socket(ipAddress,port);
                ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());
                User user = loginFrm.getLoginUser();
                if(user!=null){
                    oos.writeObject(user);
                    oos.flush();
                    String msg = (String)ois.readObject();
                    if(msg.equals("OK")) JOptionPane.showMessageDialog(null, "login successfully!");
                    else if(msg.equals("NOTFOUND")) JOptionPane.showMessageDialog(null, "no such account!");
                }
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }finally{
                try{
                    if(mySocket != null) mySocket.close();
                }catch(IOException ex){}
            }
        }
    }

    public class SignUpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
           try{
                mySocket = new Socket(ipAddress,port);
                ObjectOutputStream oos = new ObjectOutputStream(mySocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(mySocket.getInputStream());
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
            }finally{
                try{
                    if(mySocket != null) mySocket.close();
                }catch(IOException ex){}
            }
        }
    }
}
