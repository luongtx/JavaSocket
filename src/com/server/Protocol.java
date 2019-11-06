package com.server;

/*
 * Protocol.java
 *
 * Created on 01 �����, 2008, 09:38 �
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Mohamed Talaat Saad
 */
//Tạo thông điệp đầy đủ để gửi cho client
public class Protocol {
    
    private String message="";
    /** Creates a new instance of Protocol */
    public Protocol() {
    }
    
    public String IDPacket(int id)
    {
        message="ID"+id;
        return message;
    }
    public String NewClientPacket(int x,int y,int dir,int id)
    {
        message="NewClient"+x+","+y+"-"+dir+"|"+id;
        return message;   
    }
    
}
