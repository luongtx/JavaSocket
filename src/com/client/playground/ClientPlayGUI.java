package com.client.playground;


import com.client.Client;
//import com.server.Protocol;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
/*
 * ClientPlayGUI.java
 *
 * Created on 21 ����, 2008, 02:26 �
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Mohamed Talaat Saad
 */
public class ClientPlayGUI extends JFrame implements ActionListener
{
    
    /** Creates a new instance of ClientPlayGUI */
    public static JPanel gameStatusPanel;
    private static JLabel scoreLabel;
    private static JLabel playerLabel;
    public static Client client;
    private Tank clientTank;
    private static JButton btnStart;
    private static JButton btnBack;
    
    private static int score;
    
    int width=790,height=580;
//    int width=500,height=300;
    boolean isRunning=true;
    private GameBoardPanel boardPanel;
    
//    private SoundManger soundManger;
    public ClientPlayGUI(Client client) 
    {
        ClientPlayGUI.client = client;
        score=0;
        setTitle("Multiclients Tanks Game");
        setSize(width,height);
        setLocation(60,100);
        getContentPane().setBackground(Color.BLACK);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        addWindowListener(new WindowListener());
        //game status panel
        gameStatusPanel=new JPanel();
        gameStatusPanel.setBackground(Color.YELLOW);
        gameStatusPanel.setSize(200,140);
        gameStatusPanel.setBounds(560,50,200,140);
        gameStatusPanel.setLayout(null);
        playerLabel = new JLabel("Player name: "+client.getCurrentUser().getUsername());
        playerLabel.setBounds(10, 30, 150, 25);
        playerLabel.setForeground(Color.blue);
        scoreLabel=new JLabel("Score : 0");
        scoreLabel.setBounds(10,60,100,25);
        scoreLabel.setForeground(Color.green);
        gameStatusPanel.add(scoreLabel);
        gameStatusPanel.add(playerLabel);
        
        btnStart = new JButton("Start");
        btnStart.setBounds(600,300,90,30);
        btnStart.addActionListener(this);
        btnStart.setFocusable(false);      
        btnBack = new JButton("Lobby");
        btnBack.setBounds(600,350,90,30);
        btnBack.addActionListener(this);
        btnBack.setFocusable(false);
        
        clientTank=new Tank();
        boardPanel=new GameBoardPanel(clientTank,client,false);
        //content pane
        getContentPane().add(gameStatusPanel);
        getContentPane().add(boardPanel);
        getContentPane().add(btnStart);
        getContentPane().add(btnBack);
        setVisible(true);
        boardPanel.setGameStatus(true);
        boardPanel.repaint();
    }
    
    public static int getScore()
    {
        return score;
    }
    
    public static void setScore(int scoreParametar)
    {
        score+=scoreParametar;
        scoreLabel.setText("Score : "+score);
    }
   

    public void windowOpened(WindowEvent e) 
    {

    }
    class WindowListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            // int response=JOptionPane.showConfirmDialog(this,"Are you sure you want to exit ?","Tanks 2D Multiplayer Game!",JOptionPane.YES_NO_OPTION);
            client.sendToServer("EXIT",Integer.toString(clientTank.getTankID()));
            isRunning = false;
        }
    }
    @Override
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        if(o==btnStart){
            btnStart.setEnabled(false);
            try {
                System.out.println("pos x: "+clientTank.getXposition());
                System.out.println("pos y: "+clientTank.getYposition());
                client.register(clientTank.getXposition(), clientTank.getYposition());
    //                 soundManger=new SoundManger();
                boardPanel.setGameStatus(true);
                boardPanel.addKeyListener();
//                boardPanel.repaint();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                ClientRecivingThread listenerThread = new ClientRecivingThread();
                listenerThread.start();
                boardPanel.setFocusable(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "The Server is not running, try again later!", "Tanks 2D Multiplayer Game", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("The Server is not running!");
            }
        }else{
            Client.roomFrm.setVisible(true);
            isRunning = false; 
            dispose();
        }
        
    }
    
    
    public class ClientRecivingThread extends Thread
    {
        DataInputStream reader;
        public ClientRecivingThread()
        {
            try{
                 reader = client.getDataInputStream();
                 System.out.println("reader: "+reader);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        public void closeReader(){
            try{
                reader.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        @Override
        public void run()
        {
            while(isRunning==true) 
            {
                String sentence="";
                try {
                    System.out.println("[receive msg from server]");
                    sentence=reader.readUTF();
                    System.out.println("sentence: "+sentence);
                    System.out.println("[received msg]");
                    if (sentence.startsWith("ID")) {
                        int id = Integer.parseInt(sentence.substring(2));
                        clientTank.setTankID(id);
                        System.out.println("My ID= " + boardPanel.getTank().getTankID());
                    } else if (sentence.startsWith("NewClient")) {
                        int pos1 = sentence.indexOf(',');
                        int pos2 = sentence.indexOf('-');
                        int pos3 = sentence.indexOf('|');
                        int x = Integer.parseInt(sentence.substring(9, pos1));
                        int y = Integer.parseInt(sentence.substring(pos1 + 1, pos2));
                        int dir = Integer.parseInt(sentence.substring(pos2 + 1, pos3));
                        int id = Integer.parseInt(sentence.substring(pos3 + 1, sentence.length()));
                        if (id != clientTank.getTankID()) {
                            boardPanel.registerNewTank(new Tank(x, y, dir, id));
                        }
                    } else if (sentence.startsWith("Update")) {
                        System.out.println(sentence);
                        String[] data = sentence.substring(6).split(",");

                        int x = Integer.parseInt(data[0]);
                        int y = Integer.parseInt(data[1]);
                        int dir = Integer.parseInt(data[2]);
                        int id = Integer.parseInt(data[3]);

                        if (id != clientTank.getTankID()) {
                            boardPanel.getTank(id).setXpoistion(x);
                            boardPanel.getTank(id).setYposition(y);
                            boardPanel.getTank(id).setDirection(dir);
                            boardPanel.repaint();
                        }

                    } else if (sentence.startsWith("Shot")) {
                        int id = Integer.parseInt(sentence.substring(4));

                        if (id != clientTank.getTankID()) {
                            boardPanel.getTank(id).Shot();
                        }

                    } else if (sentence.startsWith("Remove")) {
                        int id = Integer.parseInt(sentence.substring(6));
                        if (id == clientTank.getTankID()) {
                            System.out.println("I'm dead");
                            int response = JOptionPane.showConfirmDialog(null, "You're lose!, back to lobby?","Java 2d tank game", JOptionPane.OK_CANCEL_OPTION);
                            if(response == JOptionPane.OK_OPTION){
                                Client.roomFrm.setVisible(true);
                                isRunning = false;
                                dispose();
                            }
//                            boardPanel.removeTank(id);
                            boardPanel.removeKeyListener();
//                            boardPanel.repaint();
                        } else {
                            boardPanel.removeTank(id);
//                            boardPanel.repaint();
                        }
                    }else if(sentence.startsWith("Win")){
                        int response = JOptionPane.showConfirmDialog(null, "You're victory!, back to lobby?","Java 2d tank game", JOptionPane.OK_CANCEL_OPTION);
                        if(response == JOptionPane.OK_OPTION){
                            Client.roomFrm.setVisible(true);
                            isRunning = false;
                            dispose();
                        }
                    }
                    else if (sentence.startsWith("Exit")) {
                        int id = Integer.parseInt(sentence.substring(4));

                        if (id != clientTank.getTankID()) {
                            boardPanel.removeTank(id);
//                            boardPanel.repaint();
                        }
                    }
                      
                } catch (IOException ex) {
                    ex.printStackTrace();
                }                
            }
        }
    }
    
}
