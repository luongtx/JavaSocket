/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.test.client;

import java.io.Serializable;

/**
 *
 * @author luongtx
 */
public class User implements Serializable{
    private String username;
    private String password;
    private String requestState;
    private boolean login;
    public User(String username, String password, String requestState) {
        this.username = username;
        this.password = password;
        this.requestState = requestState;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
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
    
    public String getRequestState(){
        return requestState;
    }
    
    public void setRequestState(String requestState){
        this.requestState = requestState;
    }
    public void setLogin(boolean login){
        this.login = login;
    }
    public boolean isLogin(){
        return login;
    }
}
