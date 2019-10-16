/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tcp.client.User;

/**
 *
 * @author luongtx
 */
public final class ServerControl {

    private ServerSocket myServer;
    private Socket socketConn;
    private Connection dbConn;
    private final int serverPort = 7777;
    private ArrayList<User> onlineUsers = new ArrayList<>();

    public ServerControl() {
        DBConnect("tcplogin", "root", "");
        listenning(serverPort);
    }

    public void DBConnect(String dbName, String username, String password) {
        String dbClass = "com.mysql.jdbc.Driver";
        String dbURL = "jdbc:mysql://localhost:3306/" + dbName;
        try {
            Class.forName(dbClass);
            dbConn = DriverManager.getConnection(dbURL, username, password);
            System.out.println("Connect DB successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void listenning(int portNumber) {
        try {
            //create(bind) server socket to listenning client on specified port
            myServer = new ServerSocket(portNumber);
            System.out.println("Server is listenning...");
            //to serve multiple clients
            while (true) {
                try {
                    //accept a client connection request
                    socketConn = myServer.accept();
                    socketConn.
                    System.out.println("server accept request");
                    ClientHandler clientHandler = new ClientHandler(socketConn);
                    clientHandler.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    break;
                } 
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    class ClientHandler extends Thread {
        Socket conn;
        ObjectInputStream ois;
        ObjectOutputStream oos;

        public ClientHandler(Socket conn) {
            this.conn = conn;
            try{
                ois = new ObjectInputStream(conn.getInputStream());
                oos = new ObjectOutputStream(conn.getOutputStream());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        @Override
        public void run(){
            String request = "";
            while (true) {
                try {
                    request = (String) ois.readObject();
                    handle(request);
                } catch (Exception ex) {
                    System.out.println("Client closed!");
                    break;
                }
            }
        }
        public void handle(String request){
            try {
                switch (request) {
                    case "LOGIN":
                        User user = (User) ois.readObject();
                        String status = checkLogin(user);
                        oos.writeObject(status);
                        oos.flush();
                        if(status.equals("OK")) {
                            System.out.println("username: " + user.getUsername());
                            if(!checkOnline(user)) onlineUsers.add(user);
                        }
                        break;
                    case "SIGNUP":
                        user = (User) ois.readObject();
//                        System.out.println("username: " + user.getUsername());
                        status = checkSignUp(user);
                        oos.writeObject(status);
                        oos.flush();
                        break;
                    case "GETONLINEUSERS":
                        oos.reset();//remove old objects in stream
                        System.out.println("online: " + onlineUsers.size());
                        oos.writeObject(onlineUsers);
                        break;
                    case "LOGOUT":
                        user = (User) ois.readObject();
                        onlineUsers.remove(user);
                        System.out.println("online: " + onlineUsers.size());
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        private boolean checkOnline(User cUser){
            for(User user: onlineUsers){
                if(user.getUsername().equals(cUser.getUsername())) return true;
            }
            return false;
        }
        private User getUserByName(String name) {
            String sql = "SELECT * FROM tblUser WHERE username = ?";
            PreparedStatement ps;
            ResultSet rs;
            User user = null;
            try {
                ps = dbConn.prepareStatement(sql);
                ps.setString(1, name);
                rs = ps.executeQuery();
                user = new User(rs.getString(1), rs.getString(2));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return user;
        }

        private String checkSignUp(User user) {
            String sql1 = "SELECT * FROM tblUser WHERE username = ?";
            String sql2 = "INSERT INTO tblUser (username, password) VALUES(?,?)";
            PreparedStatement ps1, ps2;
            ResultSet rs;
            String check = "";
            try {
                ps1 = dbConn.prepareStatement(sql1);
                ps1.setString(1, user.getUsername());
                rs = ps1.executeQuery();

                if (rs.next()) {
                    check = "EXISTED";
                } else {
                    ps2 = dbConn.prepareStatement(sql2);
                    ps2.setString(1, user.getUsername());
                    ps2.setString(2, user.getUsername());
                    ps2.executeUpdate();
                    check = "OK";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return check;
        }

        private String checkLogin(User user) {
            String sql = "SELECT * FROM tblUser WHERE username = ? && password = ?";
            PreparedStatement ps;
            ResultSet rs;
            String check = "";
            try {
                ps = dbConn.prepareStatement(sql);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                rs = ps.executeQuery();
                if (rs.next()) {
                    check = "OK";
                    user.setLogin(true);
                } else {
                    check = "NOTFOUND";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return check;
        }
    }

}

