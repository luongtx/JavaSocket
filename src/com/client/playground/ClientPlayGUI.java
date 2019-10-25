package com.client.playground;


import com.client.Client;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
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
public class ClientPlayGUI extends JFrame
{
    
    /** Creates a new instance of ClientPlayGUI */
    public static JPanel gameStatusPanel;
    private static JLabel scoreLabel;
    public static Client client;
    private Tank clientTank;
    
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
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        addWindowListener(new WindowListener());
        //game status panel
        gameStatusPanel=new JPanel();
        gameStatusPanel.setBackground(Color.YELLOW);
        gameStatusPanel.setSize(200,140);
        gameStatusPanel.setBounds(560,50,200,140);
        gameStatusPanel.setLayout(null);
        scoreLabel=new JLabel("Score : 0");
        scoreLabel.setBounds(10,90,100,25);
        gameStatusPanel.add(scoreLabel);
        
        //player state panel
//        gameStatusPanel=new JPanel();
//        gameStatusPanel.setBackground(Color.YELLOW);
//        gameStatusPanel.setSize(200,300);
//        gameStatusPanel.setBounds(560,210,200,311);
//        gameStatusPanel.setLayout(null);
//       
//        scoreLabel=new JLabel("Score : 0");
//        scoreLabel.setBounds(10,90,100,25);
//       
//       
//        gameStatusPanel.add(scoreLabel);
            
        clientTank=new Tank();
        boardPanel=new GameBoardPanel(clientTank,client,false);
        register();
        //content pane
        getContentPane().add(gameStatusPanel);
        getContentPane().add(boardPanel);        
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
            client.sendToServer("EXIT",new Protocol().ExitMessagePacket(clientTank.getTankID()));
        }
    }
    public void register(){
        try {
            System.out.println("pos x: "+clientTank.getXposition());
            System.out.println("pos y: "+clientTank.getYposition());
            client.register(clientTank.getXposition(), clientTank.getYposition());
//                 soundManger=new SoundManger();
            boardPanel.setGameStatus(true);
            boardPanel.repaint();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            new ClientRecivingThread(client.getSocket()).start();
            boardPanel.setFocusable(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "The Server is not running, try again later!", "Tanks 2D Multiplayer Game", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("The Server is not running!");
        }
    }
    
    public class ClientRecivingThread extends Thread
    {
        Socket socket;
        DataInputStream reader;
        public ClientRecivingThread(Socket socket)
        {
            this.socket=socket;
            try{
                 reader = new DataInputStream(socket.getInputStream());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        @Override
        public void run()
        {
            while(isRunning) 
            {
                String sentence="";
                try {
                    System.out.println("[receive msg from server]");
                    sentence=reader.readUTF();
                    System.out.println("[received msg]");
                    if (sentence.startsWith("ID")) {
                        int id = Integer.parseInt(sentence.substring(2));
                        clientTank.setTankID(id);
                        System.out.println("My ID= " + clientTank.getTankID());

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
                        System.out.println("died: " + id);
                        if (id == clientTank.getTankID()) {
                            int response = JOptionPane.showConfirmDialog(null, "Sorry, You are loss. Do you want to try again ?", "Tanks 2D Multiplayer Game", JOptionPane.OK_CANCEL_OPTION);
                            if (response == JOptionPane.OK_OPTION) {
                                //client.closeAll();
                                setVisible(false);
                                dispose();
                                new ClientPlayGUI(client);
                            } else {
                                System.exit(0);
                            }
                        } else {
                            boardPanel.removeTank(id);
                        }
                    } else if (sentence.startsWith("Exit")) {
                        int id = Integer.parseInt(sentence.substring(4));

                        if (id != clientTank.getTankID()) {
                            boardPanel.removeTank(id);
                        }
                    }
                      
                } catch (IOException ex) {
                    ex.printStackTrace();
                }                
               
            }
            try {
                reader.close();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
}