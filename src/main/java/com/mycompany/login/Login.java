/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.login;

import javax.swing.JFrame;

/**
 *
 * @author PC2
 */
public class Login {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello World!");
        System.out.println("STARTING.......");
        Thread.sleep(1000);
        System.out.println("1");
        Thread.sleep(1000);
        System.out.println("2");
        Thread.sleep(1000);
        System.out.println("3");
        System.out.println("PROGRAM STARTED.");
//        LoginInterface LoginFrame = new LoginInterface();
//        LoginFrame.setVisible(true);
//        LoginFrame.pack();
        MainFrame m = new MainFrame();
        m.setVisible(true);
    }
}
