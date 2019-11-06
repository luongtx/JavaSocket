/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.test.client;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author luongtx
 */
public class Sounds {

//    public static synchronized void playSound(final String url) {
//        new Thread(new Runnable() {
//            // The wrapper thread is unnecessary, unless it blocks on the
//            // Clip finishing; see comments.
//            @Override
//            public void run() {
//                try {
//                    String soundPath;
//                    Clip clip = AudioSystem.getClip();
//                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
//                            Main.class.getResourceAsStream("./" + url));
//                    clip.open(inputStream);
//                    clip.start();
//                } catch (Exception e) {
//                    System.err.println(e.getMessage());
//                }
//            }
//        }).start();
//    }
    static void playSound(String soundFile) {
        try {
            File f = new File("./" + soundFile);
            while(true){
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                Thread.sleep(1000);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {
        playSound("sounds/tank_drill.wav");
    }
}
