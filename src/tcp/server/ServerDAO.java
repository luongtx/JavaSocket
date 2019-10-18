/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.server;

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
public class ServerDAO {
    protected static Connection dbConn;
    public ServerDAO(){
        DBConnect("tcplogin", "root", "");
    }
    public void DBConnect(String dbName, String username, String password) {
        if(dbConn==null){
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
    }
    public Connection getConnection(){
        return dbConn;
    }
    
    public boolean addUserAccount(User user){
         String sql1 = "SELECT * FROM tblUser WHERE username = ?";
         String sql2 = "INSERT INTO tblUser (username, password) VALUES(?,?)";
         PreparedStatement ps1, ps2;
         ResultSet rs;
         boolean check = false;
         try {
             ps1 = dbConn.prepareStatement(sql1);
             ps1.setString(1, user.getUsername());
             rs = ps1.executeQuery();

             if (rs.next()) {
                 check = false;
             } else {
                 ps2 = dbConn.prepareStatement(sql2);
                 ps2.setString(1, user.getUsername());
                 ps2.setString(2, user.getUsername());
                 ps2.executeUpdate();
                 check = true;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return check;
     }
     public boolean deleteUser(String username){
         String sql = "DELETE FROM tbluser WHERE username=?";
         PreparedStatement ps;
         boolean check = false;
         try{
             ps = dbConn.prepareStatement(sql);
             ps.setString(1, username);
             ps.executeUpdate();
             check = true;
         }catch(Exception ex){
             ex.printStackTrace();
             check = false;
         }
         return check;
     }
     public ArrayList<User> getAllUsers(){
         ArrayList<User> userList = new ArrayList<>();
         String sql = "SELECT * FROM tbluser";
         PreparedStatement ps;
         ResultSet rs;
         try{
             ps = dbConn.prepareStatement(sql);
             rs = ps.executeQuery();
             User user;
             while(rs.next()){
                 user = new User(rs.getString("username"), rs.getString("password"), rs.getInt("win"), rs.getInt("lose"), rs.getInt("score"));
                 userList.add(user);
             }
         }catch(Exception ex){
             ex.printStackTrace();
         }
         return userList;
     }
    public boolean getUserAccount(User user) {
        String sql = "SELECT * FROM tblUser WHERE username = ? && password = ?";
        PreparedStatement ps;
        ResultSet rs;
        boolean check = false;
        try {
            ps = dbConn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            rs = ps.executeQuery();
            if (rs.next()) {
                check = true;
                user.setLogin(true);
            } else {
                check = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return check;
    }
}
