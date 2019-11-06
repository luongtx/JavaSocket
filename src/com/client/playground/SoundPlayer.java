/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.client.playground;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 *
 * @author luongtx
 */
//Hiệu ứng âm thanh
public class SoundPlayer {
    File soundFile;
    public SoundPlayer(String fileName){
        soundFile = new File("./sounds/"+fileName);
    }
    public void playSound() {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile.toURI().toURL());
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
}
