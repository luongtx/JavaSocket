/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import com.client.lobby.ClientLoginFrm;
import com.client.lobby.ClientRoomFrm;
import com.client.playground.ClientPlayGUI;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author luongtx
 */
public class Client {

    private Socket mySocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataInputStream dis;
    private DataOutputStream dos;
    private static String serverAddress = "192.168.0.107";
    private static int serverPort = 7777;
    private DatagramSocket udpSocket;
    private DatagramPacket sendPk, receivePk;
    private User currentUser;//Người dùng hiện tại
    private static ArrayList<User> loggedUsers;//Danh sách người chơi đã đăng nhập
    public static ArrayList<Room> roomList;//Danh sách phòng
    private ClientLoginFrm loginFrm;//Giao diện đăng nhập
    private ClientRoomFrm roomFrm;// Giao diện sảnh đợi
    private ClientPlayGUI mainGUI;// Giao diện chơi game
    public Client(){
        currentUser = new User();
        connectServer();
        openUDPSocket();
        initUI();
        listenRequest();
    }
    //khởi tạo giao diện
    public void initUI(){
        loginFrm = new ClientLoginFrm(this);
        loginFrm.setVisible(true);
    }
    //kết nối server, khởi tạo luồng đọc ghi
    public void connectServer(){
        if(mySocket==null){
            try{
                mySocket = new Socket(serverAddress, serverPort);
                oos = new ObjectOutputStream(mySocket.getOutputStream());
                ois = new ObjectInputStream(mySocket.getInputStream());
                dos = new DataOutputStream(mySocket.getOutputStream());
                dis = new DataInputStream(mySocket.getInputStream());
//                System.out.println("local socket: "+mySocket.getLocalSocketAddress());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    //UDPsocket để giao tiếp với các client khác
    public void openUDPSocket(){
        try{
            udpSocket = new DatagramSocket();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    //đóng kết nối tới server và các luồng đọc ghi
    public void closeConnection(){
        if(mySocket!=null){
            try{
                oos.close();
                ois.close();
                dos.close();
                dis.close();
                mySocket.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    //trả về đối tượng user theo tên
    public User getUserByName(String userName){
        for(User user: loggedUsers){
            if(user.getUsername().equals(userName)){
                return user;
            }
        }
        return null;
    }
    //đăng nhập
    public void login(User user) {
        try {
            //Lấy danh sách người dùng đăng nhập
            loggedUsers = getOnlineUsers();
            //Nếu người dùng đã có trong ds đăng nhập
            try{
                if(getUserByName(user.getUsername())!=null) {
                    JOptionPane.showMessageDialog(roomFrm, "This account currently logged in!");
                    return;
                }
            }catch(Exception ex){
                
            }
            //Gán địa chỉ ip và port cho người dùng
            if(getUserByName(user.getUsername())==null){
                user.setIpAddress(InetAddress.getLocalHost());
                user.setPort(udpSocket.getLocalPort());
            }
            //Gửi yêu cầu login tới server
            dos.writeUTF("LOGIN");
            oos.writeObject(user);
            String msg = dis.readUTF();
            System.out.println("login stt: "+msg);
            //Nhận về trạng thái đăng nhập
            if (msg.equals("OK")) {
                currentUser = user;
                System.out.println("user: "+currentUser.getUsername());
                System.out.println("ip: "+currentUser.getIpAddress());
                System.out.println("port: "+currentUser.getPort());
                roomFrm = new ClientRoomFrm(this);
                roomFrm.setVisible(true);
//                loginFrm.setVisible(false);
                loginFrm.dispose();
            }else{
                JOptionPane.showMessageDialog(loginFrm, "Wrong username or password!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //đăng xuất
    public void logout(){
        try{
            currentUser.setLogin(false);
            dos.writeUTF("LOGOUT");
            oos.writeObject(currentUser);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    //đăng ký
    public boolean signUp(User user) {
        try {
            dos.writeUTF("SIGNUP");
            oos.writeObject(user);
            String msg = (String) dis.readUTF();
            if (msg.equals("OK")) return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
    //gửi yêu cầu thách đấu tới đối thủ theo id
    public void requestSolo(int enemyId){
        try {
            String req = "SOLO " + currentUser.getUsername();
            byte[] send_buf = req.getBytes();
            //lấy thông tin đối thủ theo id
            User enemy = loggedUsers.get(enemyId);
            //nếu tự gửi yêu cầu thách đấu tới chính mình
            if(currentUser.getUsername().equals(enemy.getUsername())){
                JOptionPane.showMessageDialog(null, "lol!");
                return;
            }
            System.out.println("enemy ip: "+enemy.getIpAddress());
            System.out.println("enemy port: "+enemy.getPort());
            //gửi thông điệp yêu cầu
            sendPk = new DatagramPacket(send_buf, send_buf.length, enemy.getIpAddress(), enemy.getPort());
            udpSocket.send(sendPk);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //kiểm tra xem người dùng đã vào nhóm chưa
    public boolean isJoined(int roomID, User user){
        Room aRoom = roomList.get(roomID);
        ArrayList<User> aList = aRoom.getUserList();
        for(User u: aList){
            if(u.getUsername().equals(user.getUsername())) return true;
        }
        return false;
    }
    //gửi yêu cầu nhập phòng
    public void requestJoinRoom(int roomID){
        try{
            //Nếu người dùng đã nhập phòng
            if(isJoined(roomID,currentUser)) {
                JOptionPane.showMessageDialog(roomFrm, "You're currently in this room!");
                return;
            }
            //lấy id của người dùng muốn nhập phòng
            int userID = loggedUsers.indexOf(getUserByName(currentUser.getUsername()));
            String msg = "JOIN " + roomID + " " + userID;
            byte[] send_buf = msg.getBytes();
            //lấy thông tin về chủ phòng
            User boss = roomList.get(roomID).getBoss();
            //gửi yêu cầu nhập phòng cho chủ phòng
            sendPk = new DatagramPacket(send_buf, send_buf.length, boss.getIpAddress(), boss.getPort());
            udpSocket.send(sendPk);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    //tạo phòng mới
    public void createRoom(Room room){
        try{
            roomList.add(room);
            dos.writeUTF("UPDATEROOMS");
            oos.writeObject(roomList);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    //xóa phòng
    public void deleteRoom(int roomID){
        try {
            //Người dùng không phải chủ phòng không có quyền xóa phòng
            User roomBoss = roomList.get(roomID).getBoss();
            if(!roomBoss.getUsername().equals(currentUser.getUsername())){
                JOptionPane.showMessageDialog(roomFrm, "You don't have permission");
                return;
            }
            roomList.remove(roomID);
            //Gửi yêu cầu cập nhật phòng lên server
            dos.writeUTF("UPDATEROOMS");
            oos.writeObject(roomList);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    // Thêm một người chơi vào phòng
    public boolean addUserToRoom(int userID, int roomID){
        Room aRoom = roomList.get(roomID);
        //nếu phòng đã đầy
        if(aRoom.getStatus().contains("full")){
            JOptionPane.showMessageDialog(roomFrm, "room full!");
            return false;
        }
        ArrayList<User> aList = aRoom.getUserList();
        User aUser = loggedUsers.get(userID);
        aList.add(aUser);
        aRoom.setUserList(aList);
        roomList.set(roomID, aRoom);
        return true;
    }
    // gửi một thông điệp tới một client xác định bởi ip và port
    public void sendToPeer(String msg, InetAddress address, int port){
        try {
            byte[] send_buf = msg.getBytes();
            sendPk = new DatagramPacket(send_buf, send_buf.length,address,port);
            udpSocket.send(sendPk);
            System.out.println("send msg: "+msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //gửi thông điệp cho tất cả các client trong một phòng nhất định
    public void sendRoomMsg(String msg, int roomID) throws InterruptedException{
        Room aRoom = roomList.get(roomID);
        ArrayList<User> roomMembers = aRoom.getUserList();
        for(User user: roomMembers){
            sendToPeer(msg, user.getIpAddress(), user.getPort());
            Thread.sleep(500);
        }
    }
    //lắng nghe yêu cầu từ các client khác
    public void listenRequest(){
        while(true){
            try{
                byte[] receive_buf = new byte[1024];
                receivePk = new DatagramPacket(receive_buf, receive_buf.length);
                udpSocket.receive(receivePk);
                String receiveMsg = new String(receivePk.getData());
                String [] data = receiveMsg.split(" ");
                String req = data[0].trim();
                //Xử lý từng request
                switch(req){
                    //Bắt đầu game
                    case "START":
//                        System.out.println("trigger start");
                        mainGUI = new ClientPlayGUI(this);
                        mainGUI.setVisible(true);
                        roomFrm.setVisible(false);
                        break;
                    //Đấu Solo 1 vs 1
                    case "SOLO":
                        String ans;
                        String rivalName = data[1].trim();
                        int input = JOptionPane.showConfirmDialog(roomFrm, "Do you want to play with "+ rivalName, "Confirm battle request", JOptionPane.OK_CANCEL_OPTION);
                        if(input==JOptionPane.OK_OPTION) {
                            ans = "REPSOLO OK";
                            mainGUI = new ClientPlayGUI(this);
                            mainGUI.setVisible(true);
                            roomFrm.setVisible(false);
                            Thread.sleep(500);
                        }else ans = "REPSOLO CANCEL";
                        //Gửi thông điệp xác nhận thách đấu cho người thách đấu
                        sendToPeer(ans, receivePk.getAddress(), receivePk.getPort());
                        break;
                    //Nhập phòng    
                    case "JOIN":
                        int roomID = Integer.parseInt(data[1].trim());
                        int userID = Integer.parseInt(data[2].trim());
                        //Lấy danh sách người chơi online (đã đăng nhập)
                        getOnlineUsers();
                        //Nếu người yêu cầu nhập phòng là chính người chơi hiện tại
                        if(loggedUsers.get(userID).getUsername().equals(currentUser.getUsername())){
                            addUserToRoom(userID, roomID);
                        }else{
                            String roomName = roomList.get(roomID).getRoomName();
                            String userName = loggedUsers.get(userID).getUsername();
                            //Gửi thông báo xác nhận cho người chơi nhập phòng
                            input = JOptionPane.showConfirmDialog(roomFrm, "Do you want "+userName+" to join room "+roomName,"Confirm join request",JOptionPane.OK_CANCEL_OPTION);
                            if(input==JOptionPane.OK_OPTION && addUserToRoom(userID, roomID)){
                                ans = "REPJOIN OK";
                            }else ans = "REPJOIN CANCEL";
                            //Gửi thông điệp xác nhận cho nhập phòng tới người yêu cầu nhập phòng
                            sendToPeer(ans, receivePk.getAddress(), receivePk.getPort());
                        }
                        //Nếu phòng đã đủ người thì thông báo cho tất cả người chơi trong phòng bắt đầu game
                        if (roomList.get(roomID).getStatus().contains("full")) {
                            System.out.println("room full!");
                            sendRoomMsg("START", roomID);
                        }
                        //Cập nhật lại bảng danh sách phòng
                        roomFrm.updateTbRoom(roomList);
                        //Gửi yêu cầu cập nhật danh sách phòng tới server và danh sách phòng tương ứng
                        dos.writeUTF("UPDATEROOMS");
                        oos.reset();
                        oos.writeObject(roomList);
//                        oos.flush();
                        break;
                    //Xác nhận cho nhập phòng
                    case "REPJOIN":
                        String msg = data[1].trim();
                        //Nếu cho nhập phòng thì hiện thông báo yêu cầu người chơi đợi cho phòng đủ người
                        if(msg.contains("OK")){
                            JOptionPane.showMessageDialog(roomFrm, "wait for battle!");
                            roomFrm.updateTbRoom(getRooms());
                        }
                        //Nếu thông điệp xác nhận cho nhập phòng là từ chối
                        else
                            JOptionPane.showMessageDialog(roomFrm, "request denied!");
                        break;
                    //Xác nhận đấu 1 vs 1
                    case "REPSOLO":
                        msg = data[1].trim();
                        //Nếu đồng ý thách đấu
                        if(msg.contains("OK")){
                            mainGUI = new ClientPlayGUI(this);
                            mainGUI.setVisible(true);
                            roomFrm.setVisible(false);
                        }else{
                            JOptionPane.showMessageDialog(roomFrm, "request denied!");
                        }
                        break;
                    default:
                        break;
                }
               
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    //Khi bắt đầu game thì gửi vị trí của người chơi lên server
    public void registerPos(int posX, int posY) throws IOException {
        dos.writeUTF("REGISTER");
        dos.writeUTF(currentUser.getUsername()+","+posX+","+posY);
    }
    //Gửi một yêu cầu tới server
    public void sendToServer(String title, String data) {
        try {
            dos.writeUTF(title);
            if (title.equals("REMOVE")) {
                data = currentUser.getUsername() + " " + data;
            }
            dos.writeUTF(data);
            System.out.println(title + data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    //Trở về sảnh
    public void returnLobby(){
        mainGUI.dispose();
        roomFrm.setVisible(true);
    }
    //Lấy danh sách người chơi đang online (đã đăng nhập) từ server
    public ArrayList<User> getOnlineUsers(){
        try {
            dos.writeUTF("GETONLINEUSERS");
            loggedUsers = (ArrayList<User>) ois.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return loggedUsers;
    }
    //Lấy danh sách phòng từ server
    public ArrayList<Room> getRooms(){
        try{
            dos.writeUTF("GETROOMS");
            roomList = (ArrayList<Room>) ois.readObject();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return roomList;
    }
    //Lấy người chơi hiện tại
    public User getCurrentUser(){
        return currentUser;
    }
    //Lấy danh sách tất cả người dùng và sắp xếp theo thứ tự điểm từ cao đến thấp (Bảng xếp hạng)
    public ArrayList<User> getLeaderBoard(){
        ArrayList<User> userList = null;
        try {
            dos.writeUTF("GETALLUSERS");
            userList = (ArrayList<User>) ois.readObject();
            Collections.sort(userList, new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    return o2.getScore() - o1.getScore();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return userList;
    }

    public Socket getSocket() {
        return mySocket;
    }
    public DataInputStream getDataInputStream(){
        return dis;
    }
    public DataOutputStream getDataOutputStream(){
        return dos;
    }
    
}
