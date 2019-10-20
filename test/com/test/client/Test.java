/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.test.client;

/**
 *
 * @author luongtx
 */
public class Test {
    public static void main(String[] args) {
        Student std1 = new Student("Luong");
        std1.setNumberofStd(15);
        System.out.println(std1.getId());
        Student std2 = new Student("ha");
        System.out.println(std2.getId());
        Student std3 = new Student("khiem");
        System.out.println(std3.getId());
    }
}
