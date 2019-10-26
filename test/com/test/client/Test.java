/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.test.client;

import java.util.ArrayList;

/**
 *
 * @author luongtx
 */
public class Test {
    Student std;
    public Test(Student std){
        this.std = std;
        std.setTutorName("luong");
    }
    public String getTutor(){
        return std.getTutorName();
    }
    public static void main(String[] args) {
        Student std1 = new Student("abc");
        Test t = new Test(std1);
        std1.setTutorName("kiem");
        System.out.println(t.getTutor());
    }
}
