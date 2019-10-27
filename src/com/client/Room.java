/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author luongtx
 */
//Thông tin về phòng
public class Room implements Serializable{
    private String roomName;//Tên phòng
    private User boss;//Chủ phòng
    private ArrayList<User> userList;//Danh sách người chơi trong phòng
    private int capacity;//Sức chứa của phòng
    public Room(String name, User boss, int capacity){
        this.roomName = name;
        this.boss = boss;
        this.capacity = capacity;
        userList = new ArrayList<>();
    }
    public void addUsers(User user){
        userList.add(user);
    }
    
    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    public User getBoss() {
        return boss;
    }

    public void setBoss(User boss) {
        this.boss = boss;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    

    public ArrayList<User> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<User> userList) {
        this.userList = userList;
    }
    public String getStatus(){
        if(userList.size()==capacity) return "full";
        return userList.size() + "/" + capacity;
    }
    public Object[] toObject(){
        return new Object[]{
            roomName,boss.getUsername(),this.getStatus()
        };
    }
    
}
