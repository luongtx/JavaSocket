/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.test.server;

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
import tcp.test.client.User;

/**
 *
 * @author luongtx
 */
public final class ServerControl1 {

    private ServerSocket myServer;
    private Socket conn;
    private Connection dbConnection;
    private final int serverPort = 7777;
    private ArrayList<User> onlineUsers = new ArrayList<>();

    public ServerControl1() {
        DBConnect("tcplogin", "root", "");
        listenning(serverPort);
    }

    public void DBConnect(String dbName, String username, String password) {
        String dbClass = "com.mysql.jdbc.Driver";
        String dbURL = "jdbc:mysql://localhost:3306/" + dbName;
        try {
            Class.forName(dbClass);
            dbConnection = DriverManager.getConnection(dbURL, username, password);
            System.out.println("Connect DB successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void listenning(int portNumber) {
        try {
            //create(bind) server socket to listenning client on specified port
            myServer = new ServerSocket(portNumber);
            System.out.println("listenning...");
            //to serve multiple clients
            while (true) {
                try {
                    //accept a client connection request
                    conn = myServer.accept();
                    System.out.println("accept!");
                    ClientHandler clHandler = new ClientHandler(conn);
                    clHandler.start();

                } catch (Exception ex) {
                    ex.printStackTrace();
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
            try {
                ois = new ObjectInputStream(conn.getInputStream());
                oos = new ObjectOutputStream(conn.getOutputStream());
                System.out.println("initiallized input and output stream");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                //login & signup
                User user = null;
                do {
                    user = (User) ois.readObject();
                    System.out.println("username: " + user.getUsername());
                    String status = getCheckingStatus(user);
                    oos.writeObject(status);
                    oos.flush();
                } while (!user.isLogin());
                onlineUsers.add(user);
                System.out.println("number user online:" + onlineUsers.size());
                String request = (String) ois.readObject();
                switch (request) {
                    case "GETONLINEUSERS":
                        oos.writeObject(onlineUsers);
                        break;
                    case "JOINROOM":
                        User opponent = getUserByName(ois.readObject().toString());
//                    dos.write("");
                    case "VIEWROOM":
                    default:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public User getUserByName(String name) {
            String sql = "SELECT * FROM tblUser WHERE username = ?";
            PreparedStatement ps;
            ResultSet rs;
            User user = null;
            try {
                ps = dbConnection.prepareStatement(sql);
                ps.setString(1, name);
                rs = ps.executeQuery();
                user = new User(rs.getString(1), rs.getString(2));
            } catch (Exception ex) {

            }
            return user;
        }

        public String getCheckingStatus(User user) {
            String sql1 = "SELECT * FROM tblUser WHERE username = ? && password = ?";
            String sql2 = "INSERT INTO tblUser (username, password) VALUES(?,?)";
            PreparedStatement ps1, ps2;
            ResultSet rs;
            String checkingStatus = "";
            try {
                ps1 = dbConnection.prepareStatement(sql1);
                ps1.setString(1, user.getUsername());
                ps1.setString(2, user.getPassword());
                rs = ps1.executeQuery();
                ps2 = dbConnection.prepareStatement(sql2);
                ps2.setString(1, user.getUsername());
                ps2.setString(2, user.getPassword());

                if (user.getRequestState().equals("login")) {
                    if (rs.next()) {
                        checkingStatus = "OK";
                    } else {
                        checkingStatus = "NOTFOUND";
                    }
                } else {
                    if (rs.next()) {
                        checkingStatus = "EXISTED";
                    } else {
                        ps2.executeUpdate();
                        checkingStatus = "OK";
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return checkingStatus;
        }
    }

}
