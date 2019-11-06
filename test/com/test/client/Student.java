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
public class Student {
    private int id;
    private String name;
    private String tutorName = "michael";
    public int numberofStd = 1;;
    public Student(String name){
        this.name = name;
        id = 1;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberofStd() {
        return numberofStd;
    }

    public void setNumberofStd(int num) {
        this.numberofStd = num;
    }
    
}
