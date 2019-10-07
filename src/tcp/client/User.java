/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp.client;

import java.io.Serializable;

/**
 *
 * @author luongtx
 */
public class User implements Serializable{
    private String username;
    private String password;
    private String requestState;

    public User(String username, String password, String requestState) {
        this.username = username;
        this.password = password;
        this.requestState = requestState;
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
    
}
