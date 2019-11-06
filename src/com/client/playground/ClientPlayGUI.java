package com.client.playground;


import com.client.Client;
//import com.server.Protocol;
import java.awt.Color;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
//Giao diện game
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
    private static JButton btnsend;
    private static List listchat;
    private static int score;
    private static JTextField chattext; // input chat
    private DataOutputStream write;
    int width=700,height=600;
//    int width=500,height=300;
    boolean isRunning=true;
    private GameBoardPanel boardPanel;
    
//    private SoundManger soundManger;
    public ClientPlayGUI(Client client) 
    {
        ClientPlayGUI.client = client;
        write=client.getDataOutputStream();
        score=0;
        setTitle("Game bắn xe tăng");
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
        playerLabel = new JLabel("Tên người chơi: "+client.getCurrentUser().getUsername());
        playerLabel.setBounds(10, 30, 150, 25);
        playerLabel.setForeground(Color.blue);
        scoreLabel=new JLabel("Điểm : 0");
        scoreLabel.setBounds(10,60,100,25);
        scoreLabel.setForeground(Color.green);
        gameStatusPanel.add(scoreLabel);
        gameStatusPanel.add(playerLabel);
        
        btnStart = new JButton("Bắt đầu");
        btnStart.setBounds(560,200,90,30);
        btnStart.addActionListener(this);
        btnStart.setFocusable(false);      
        btnBack = new JButton("Trở về");
        btnBack.setBounds(560,250,90,30);
        btnBack.addActionListener(this);
        btnBack.setFocusable(false);
        // tao input chat
        chattext=new JTextField();
        chattext.setBounds(560, 400, 200, 30);
        chattext.addActionListener(this);
        chattext.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                chattext.setFocusable(true);
            }
        });
        
        // tao button gui tin nhan
        btnsend=new JButton("Gửi");
        btnsend.setBounds(560, 450, 90, 30);
        btnsend.addActionListener(this);
        btnsend.setFocusable(false);
        // tao list chat
        listchat=new List();
        listchat.setBounds(560, 300, 200, 100);
        listchat.addActionListener(this);
        listchat.setFocusable(false);
        //het
        clientTank=new Tank();
        boardPanel=new GameBoardPanel(clientTank,client,false);
        //content pane
        getContentPane().add(gameStatusPanel);
        getContentPane().add(boardPanel);
        getContentPane().add(btnStart);
        getContentPane().add(btnBack);
        getContentPane().add(btnsend);
        getContentPane().add(listchat);
        getContentPane().add(chattext);
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
        scoreLabel.setText("Điểm : "+score);
    }
   

    class WindowListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            int response=JOptionPane.showConfirmDialog(rootPane,"Bạn muốn thoát?","Game đấu tank 2D!",JOptionPane.OK_CANCEL_OPTION);
            if(response==JOptionPane.OK_OPTION){
                isRunning = false;
                client.sendToServer("EXIT",Integer.toString(clientTank.getTankID()));
                client.logout();
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }else{
                setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        }
    }
    @Override
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        //khi click vào nút start
        if(o==btnStart){
            btnStart.setEnabled(false);
            try {
                System.out.println("pos x: "+clientTank.getXposition());
                System.out.println("pos y: "+clientTank.getYposition());
                client.registerPos(clientTank.getXposition(), clientTank.getYposition());
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
                JOptionPane.showMessageDialog(this, "Máy chủ ko hoạt động, thử lại sau!", "Game đấu tank 2D", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Máy chủ ko hoạt động!");
            }
        }
        //khi click vào lobby
        if(o==btnBack){
            int response=JOptionPane.showConfirmDialog(rootPane,"Bạn muốn thoát?, dữ liệu hiện tại sẽ bị mất?","Game đấu tank 2D!",JOptionPane.OK_CANCEL_OPTION);
            if(response==JOptionPane.OK_OPTION){
                isRunning = false;
                client.sendToServer("EXIT",Integer.toString(clientTank.getTankID()));
                client.returnLobby();
            }
        }
        if(o==btnsend){
            
            String text=chattext.getText();
            //listchat.add(text);
            try {
                write.writeUTF(text);
            } catch (IOException ex) {
                Logger.getLogger(ClientPlayGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            chattext.setText(" ");
            chattext.setFocusable(false);
        }
    }
    
    //tạo luồng để đọc dữ liệu từ server và hiển thị lên giao diện
    public class ClientRecivingThread extends Thread
    {
        DataInputStream reader;
        DataOutputStream write;
        public ClientRecivingThread()
        {
            try{
                 reader = client.getDataInputStream();
                 
//                 System.out.println("reader: "+reader);
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
//                    System.out.println("[receive msg from server]");
                    sentence=reader.readUTF();
//                    System.out.println("sentence: "+sentence);
                    if (sentence.startsWith("ID")) {//Khi server trả về id cho client
                        int id = Integer.parseInt(sentence.substring(2));
                        clientTank.setTankID(id);
                        System.out.println("My ID= " + boardPanel.getTank().getTankID());
                    } else if (sentence.startsWith("NewClient")) {// Khi server gửi thông điệp cập nhật người chơi mới
                        int pos1 = sentence.indexOf(',');
                        int pos2 = sentence.indexOf('-');
                        int pos3 = sentence.indexOf('|');
                        //tọa độ, hướng di chuyển và id của người chơi (tank)
                        int x = Integer.parseInt(sentence.substring(9, pos1));
                        int y = Integer.parseInt(sentence.substring(pos1 + 1, pos2));
                        int dir = Integer.parseInt(sentence.substring(pos2 + 1, pos3));
                        int id = Integer.parseInt(sentence.substring(pos3 + 1, sentence.length()));
                        //tạo một đối tượng tank mới và thêm vào gameboard
                        if (id != clientTank.getTankID()) {
                            boardPanel.registerNewTank(new Tank(x, y, dir, id));
                        }
                    } else if (sentence.startsWith("Update")) {//Server thông báo cập nhật vị trí của người chơi
                        System.out.println(sentence);
                        String[] data = sentence.substring(6).split(",");
                        int x = Integer.parseInt(data[0]);
                        int y = Integer.parseInt(data[1]);
                        int dir = Integer.parseInt(data[2]);
                        int id = Integer.parseInt(data[3]);
                        //Cập nhật vị trí của tank trên gameboard
                        if (id != clientTank.getTankID()) {
                            boardPanel.getTank(id).setXpoistion(x);
                            boardPanel.getTank(id).setYposition(y);
                            boardPanel.getTank(id).setDirection(dir);
                            boardPanel.repaint();
                        }
                      
                    } else if (sentence.startsWith("Shot")) {//Server thông báo cập nhật bomb
                        int id = Integer.parseInt(sentence.substring(4));

                        if (id != clientTank.getTankID()) {
                            boardPanel.getTank(id).Shot();
                        }

                    } else if (sentence.startsWith("Remove")) {//Server thông báo xóa tank bị trúng bomb
                        int id = Integer.parseInt(sentence.substring(6));
                        if (id == clientTank.getTankID()) {//Nếu người chơi hiện tại bị trúng bomb
//                            System.out.println("I'm dead");
                            int response = JOptionPane.showConfirmDialog(null, "Bạn đã chết!, trở về sảnh?","Game đấu tank 2D", JOptionPane.OK_CANCEL_OPTION);
                            if(response == JOptionPane.OK_OPTION){
                                isRunning = false;
                                client.sendToServer("EXIT",Integer.toString(clientTank.getTankID()));
                                client.returnLobby();
                            }
                            //không cho người chơi nhấn phím nữa
                            boardPanel.removeKeyListener();
//                            boardPanel.repaint();
                        } else {//Nếu người chơi khác bị trúng bomb
                            boardPanel.removeTank(id);
//                            boardPanel.repaint();
                        }
                    }else if(sentence.startsWith("Win")){//Thông báo win game
                        int response = JOptionPane.showConfirmDialog(null, "Bạn đã chiến thắng!, trở về sảnh?","Game đấu tank 2D", JOptionPane.OK_CANCEL_OPTION);
                        if(response == JOptionPane.OK_OPTION){
                            isRunning = false;
                            client.sendToServer("EXIT",Integer.toString(clientTank.getTankID()));
                            client.returnLobby();
                        }
                        boardPanel.removeKeyListener();
                    }
                    else if (sentence.startsWith("Exit")) {//Thông báo người chơi thoát game
                        int id = Integer.parseInt(sentence.substring(4));

                        if (id != clientTank.getTankID()) {
                            boardPanel.removeTank(id);
//                            boardPanel.repaint();
                        }
                    }
                    // nhan tin nhan cua sever
                    else{
                        listchat.add(sentence);
                    }
                      
                } catch (IOException ex) {
                    ex.printStackTrace();
                }                
            }
        }
    }
    
}
