package com.client.playground;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;

/*
 * Bomb.java
 *
 * Created on 29 ����, 2008, 06:20 �
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Mohamed Talaat Saad
 */
//Để tạo đối tượng bomb
public class Bomb {

    /**
     * Creates a new instance of Bomb
     */
    private Image bombImg;
    private BufferedImage bombBuffImage;

    private int xPosi;
    private int yPosi;
    private int direction;
    public boolean stop = false;
    private float velocityX = 0.05f, velocityY = 0.05f;
    final SoundPlayer sound_boom = new SoundPlayer("lazerboom.wav");
    final SoundPlayer sound_explode = new SoundPlayer("explosion.wav");
    public Bomb(int x, int y, int direction) {
        xPosi = x;
        yPosi = y;
        this.direction = direction;
        stop = false;
        bombImg = new ImageIcon("Images/bomb.png").getImage();

        bombBuffImage = new BufferedImage(bombImg.getWidth(null), bombImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
        bombBuffImage.createGraphics().drawImage(bombImg, 0, 0, null);
        Thread t = new Thread(() -> {
            sound_boom.playSound();
        });
        t.start();
    }

    public int getPosiX() {
        return xPosi;
    }

    public int getPosiY() {
        return yPosi;
    }

    public void setPosiX(int x) {
        xPosi = x;
    }

    public void setPosiY(int y) {
        yPosi = y;
    }

    public BufferedImage getBomBufferdImg() {
        return bombBuffImage;
    }

    public BufferedImage getBombBuffImage() {
        return bombBuffImage;
    }
    //Xử lý xung đột giữa bomb và tank
    public boolean checkCollision() {
        ArrayList<Tank> clientTanks = GameBoardPanel.getClients();
        int x, y;
        for (int i = 1; i < clientTanks.size(); i++) {
            if (clientTanks.get(i) != null) {
                x = clientTanks.get(i).getXposition();
                y = clientTanks.get(i).getYposition();
                //Khi bomb đã va chạm vào tank
                if ((yPosi >= y && yPosi <= y + 43) && (xPosi >= x && xPosi <= x + 43)) {

                    ClientPlayGUI.setScore(50);

                    ClientPlayGUI.gameStatusPanel.repaint();

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    //Kiểm tra xem tank còn sống không
                    if (clientTanks.get(i) != null) {
                        sound_explode.playSound();
                        ClientPlayGUI.client.sendToServer("REMOVE", Integer.toString(clientTanks.get(i).getTankID()));
                    }

                    return true;
                }
            }
        }
        return false;
    }
    
    public void startBombThread(boolean chekCollision) {

        new BombShotThread(chekCollision).start();

    }
    private class BombShotThread extends Thread {

        boolean checkCollis;

        public BombShotThread(boolean chCollision) {
            checkCollis = chCollision;
        }

        @Override
        public void run() {
            if (checkCollis) {

                switch (direction) {
                    case 1:
                        xPosi = 17 + xPosi;
                        while (yPosi > 50) {
                            yPosi = (int) (yPosi - yPosi * velocityY);
                            if (checkCollision()) {
                                break;
                            }
                            try {

                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    case 2:
                        yPosi = 17 + yPosi;
                        xPosi += 30;
                        while (xPosi < 564) {
                            xPosi = (int) (xPosi + xPosi * velocityX);
                            if (checkCollision()) {
                                break;
                            }
                            try {

                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    case 3:
                        yPosi += 30;
                        xPosi += 20;
                        while (yPosi < 505) {
                            yPosi = (int) (yPosi + yPosi * velocityY);
                            if (checkCollision()) {
                                break;
                            }
                            try {

                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    case 4:
                        yPosi = 21 + yPosi;
                        while (xPosi > 70) {
                            xPosi = (int) (xPosi - xPosi * velocityX);
                            if (checkCollision()) {
                                break;
                            }
                            try {

                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    default:
                        break;
                }

                stop = true;
            } else {
                switch (direction) {
                    case 1:
                        xPosi = 17 + xPosi;
                        while (yPosi > 50) {
                            yPosi = (int) (yPosi - yPosi * velocityY);

                            try {

                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    case 2:
                        yPosi = 17 + yPosi;
                        xPosi += 30;
                        while (xPosi < 564) {
                            xPosi = (int) (xPosi + xPosi * velocityX);

                            try {

                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    case 3:
                        yPosi += 30;
                        xPosi += 20;
                        while (yPosi < 505) {
                            yPosi = (int) (yPosi + yPosi * velocityY);

                            try {

                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    case 4:
                        yPosi = 21 + yPosi;
                        while (xPosi > 70) {
                            xPosi = (int) (xPosi - xPosi * velocityX);

                            try {

                                Thread.sleep(40);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                        }
                        break;
                    default:
                        break;
                }

                stop = true;
            }
        }
    }
}
