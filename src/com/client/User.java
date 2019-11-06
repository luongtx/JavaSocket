/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client;

import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author luongtx
 */
//Thông tin về người dùng
public class User implements Serializable{
    private String username;
    private String password;
    private int win;//số trận thắng
    private int lose;//số trận thua
    private int score;//tổng điểm
    private boolean login = false;//trạng thái đăng nhập
    private InetAddress ipAddress;
    private int port;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, int win, int lose, int score) {
        this.username = username;
        this.password = password;
        this.win = win;
        this.lose = lose;
        this.score = score;
    }

    public User() {

    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setLogin(boolean login){
        this.login = login;
    }
    
    public boolean isLogin(){
        return login;
    }
    
    public int getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public int getLose() {
        return lose;
    }

    public void setLose(int lose) {
        this.lose = lose;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Object[] toObject() {
        return new Object[]{username,password,win,lose,score};
    }
    
}
