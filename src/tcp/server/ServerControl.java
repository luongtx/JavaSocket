/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tcp.client.User;

/**
 *
 * @author luongtx
 */
public final class ServerControl {
    private ServerSocket myServer;
    private Socket socketConnection;
    private Connection dbConnection;
    private final int serverPort = 5555;
    public ServerControl(){
        DBConnect("tcplogin", "root", "");
        listenning(serverPort);
    }
    public void DBConnect(String dbName, String username, String password){
        String dbClass = "com.mysql.jdbc.Driver";
        String dbURL = "jdbc:mysql://localhost:3306/" + dbName;
        try{
            Class.forName(dbClass);
            dbConnection = DriverManager.getConnection(dbURL, username, password);
            System.out.println("Connect DB successfully!");
        }catch(ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
    }
    
    public void listenning(int portNumber){
        try {
            //create(bind) server socket to listenning client on specified port
            myServer = new ServerSocket(portNumber);
            System.out.println("Server is listenning...");
            //to serve multiple clients
            while(true){
                try {
                    //accept a client connection request
                    socketConnection = myServer.accept();
                    ObjectInputStream ois = new ObjectInputStream(socketConnection.getInputStream());
                    ObjectOutputStream oos = new ObjectOutputStream(socketConnection.getOutputStream());

                    User user = (User) ois.readObject();
                    String status = getCheckingStatus(user);
                    oos.writeObject(status);
                    oos.flush();
                } catch (IOException ex) {

                } finally {
                    if (socketConnection != null) {
                        socketConnection.close();
                    }
                }
            }

        }catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                if(myServer !=null) myServer.close();
                System.out.println("Server is closed");
            } catch (IOException ex) {
                Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    private String getCheckingStatus(User user) {
        String sql1 = "SELECT * FROM tblUser WHERE username = ? && password = ?";
        String sql2 = "INSERT INTO tblUser (username, password) VALUES(?,?)";
        PreparedStatement ps1,ps2;
        ResultSet rs;
        String checkingStatus = "";
        try{
            ps1 = dbConnection.prepareStatement(sql1);
            ps1.setString(1, user.getUsername());
            ps1.setString(2, user.getPassword());
            rs = ps1.executeQuery();
            ps2 = dbConnection.prepareStatement(sql2);
            ps2.setString(1, user.getUsername());
            ps2.setString(2, user.getPassword());
            
            if(user.getRequestState().equals("login")){
               if(rs.next()) checkingStatus = "OK";
               else checkingStatus =  "NOTFOUND";
            }else{
                if(rs.next()) checkingStatus = "EXISTED";
                else{
                    ps2.executeUpdate();
                    checkingStatus = "OK";
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
       return checkingStatus;
    }
}
