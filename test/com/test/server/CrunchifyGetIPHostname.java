/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.test.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 *
 * @author luongtx
 */
public class CrunchifyGetIPHostname {
    public static void main(String[] args) {
        System.out.println(getPrivateAddress());
    }
    public static InetAddress getPrivateAddress(){
        InetAddress inet = null;
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if(i.isSiteLocalAddress()) return inet = i;
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return inet;
    }
}
