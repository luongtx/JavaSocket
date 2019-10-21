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
    public static void main(String[] args) {
        ArrayList<Student> stdList = new ArrayList<>();
        stdList.add(new Student("Luong"));
        stdList.add(new Student("khiem"));
        stdList.add(new Student("kiem"));
        stdList.remove(new Student("Luong"));
        System.out.println(stdList.size());
    }
}
