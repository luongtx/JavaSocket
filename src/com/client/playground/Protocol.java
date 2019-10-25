package com.client.playground;

/*
 * Protocol.java
 *
 * Created on 25 ����, 2008, 10:32 �
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Mohamed Talaat Saad
 */
public class Protocol {
    
    
    /**
     * Creates a new instance of Protocol
     */
    private String message="";
    public Protocol() 
    {
        
    }
    
    public String RegisterPacket(int x,int y)
    {
        message=x+","+y;
        return message;
    }
    public String UpdatePacket(int x,int y,int id,int dir)
    {
        message=+x+","+y+","+dir+","+id;
        return message;
    }
    public String ShotPacket(int id)
    {
        message=Integer.toString(id);
        return message;
    }
    
    public String RemoveClientPacket(int id)
    {
        message=Integer.toString(id);
        return message;
    }
    public String ExitMessagePacket(int id)
    {
        message=Integer.toString(id);
        return message;
    }
}
