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
        ArrayList<Student> stdList = new ArrayList<>();
        stdList.add(new Student("luong"));
        stdList.add(new Student("kiem"));
        stdList.add(new Student("khiem"));
//        stdList = new ArrayList<>();
        for(int i=0;i<stdList.size();i++){
            System.out.println(stdList.get(i).getName());
        }
    }
}
