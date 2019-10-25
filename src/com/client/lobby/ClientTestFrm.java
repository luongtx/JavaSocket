/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client.lobby;

import com.client.Client;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author luongtx
 */
public class ClientTestFrm extends JFrame{
    JPanel panel1;
    JTextField userName;
    JTextField password;
    JTextArea comment;
    Client client;
    public ClientTestFrm(Client testClient){
        this.client = testClient;
        panel1 = new JPanel();
        userName = new JTextField(client.getCurrentUser().getUsername());
        password = new JTextField(client.getCurrentUser().getPassword());
        comment = new JTextArea();
        panel1.add(userName);
        panel1.add(password);
        panel1.add(comment);
        this.setLayout(new FlowLayout());
        this.add(panel1);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        System.out.println(client.getOnlineUsers().size());
        System.out.println(client.getRooms().size());
    }
}
