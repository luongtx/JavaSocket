package com.client.playground;

import com.client.Client;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
/*
 * InputManager.java
 *
 * Created on 25 ����, 2008, 02:57 �
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Mohamed Talaat Saad
 */
public class InputManager implements KeyListener  
{
    private final int LEFT = 37;
    private  final int RIGHT = 39;
    private final int UP = 38;
    private final int DOWN = 40;
    private static int status=0;    
    
    private Tank tank;
    private Client client;
    /** Creates a new instance of InputManager */
    public InputManager(Tank tank) 
    {
        this.client=ClientPlayGUI.client;
        this.tank=tank;
        
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) 
    {

        if(e.getKeyCode()==LEFT)
        {
            if(tank.getDirection()==1|tank.getDirection()==3)
            {
                
                tank.moveLeft();
                client.sendToServer("UPDATE",new Protocol().UpdatePacket(tank.getXposition(),
                          tank.getYposition(),tank.getTankID(),tank.getDirection()));
                
 
            }
            else if(tank.getDirection()==4)
            {
                tank.moveLeft();          
                client.sendToServer("UPDATE",new Protocol().UpdatePacket(tank.getXposition(),
                            tank.getYposition(),tank.getTankID(),tank.getDirection()));
            }
        }
        else if(e.getKeyCode()==RIGHT)
        {
            if(tank.getDirection()==1|tank.getDirection()==3)
            {
                tank.moveRight();                        
                client.sendToServer("UPDATE",new Protocol().UpdatePacket(tank.getXposition(),
                           tank.getYposition(),tank.getTankID(),tank.getDirection()));
                    
            }
            else if(tank.getDirection()==2)
            {
                tank.moveRight();
                client.sendToServer("UPDATE",new Protocol().UpdatePacket(tank.getXposition(),
                             tank.getYposition(),tank.getTankID(),tank.getDirection()));
            }
        }
        else if(e.getKeyCode()==UP)
        {
            if(tank.getDirection()==2|tank.getDirection()==4)
            {
                tank.moveForward();                            
                client.sendToServer("UPDATE",new Protocol().UpdatePacket(tank.getXposition(),
                          tank.getYposition(),tank.getTankID(),tank.getDirection()));
                        
            }
            else if(tank.getDirection()==1)
            {
                tank.moveForward();
                client.sendToServer("UPDATE",new Protocol().UpdatePacket(tank.getXposition(),
                        tank.getYposition(),tank.getTankID(),tank.getDirection()));
                            
            }
        }
        else if(e.getKeyCode()==DOWN)
        {
            if(tank.getDirection()==2|tank.getDirection()==4)
            {
                tank.moveBackward();
               
                client.sendToServer("UPDATE",new Protocol().UpdatePacket(tank.getXposition(),
                        tank.getYposition(),tank.getTankID(),tank.getDirection()));
                            
            }
            else if(tank.getDirection()==3)
            {
                tank.moveBackward();
                client.sendToServer("UPDATE",new Protocol().UpdatePacket(tank.getXposition(),
                                tank.getYposition(),tank.getTankID(),tank.getDirection()));
                                
            }
        }
        else if(e.getKeyCode()==KeyEvent.VK_SPACE)
        {
            client.sendToServer("SHOT",new Protocol().ShotPacket(tank.getTankID()));
            tank.shot();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    
}
