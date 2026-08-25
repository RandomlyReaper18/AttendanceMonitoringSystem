/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.login;

import javax.swing.JFrame;
import javax.swing.*;
import java.net.ServerSocket;
/**
 *
 * @author PC2
 */
public class Login {
    private static ServerSocket socketLock;
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello World!");
        System.out.println("STARTING.......");
        
        // Enforce single instance using a local port lock (e.g., 9999)
        try {
            socketLock = new ServerSocket(9999);
        } catch (Exception e) {
            // Port is already occupied by a running instance
            JOptionPane.showMessageDialog(null, 
                "Application is already running!", 
                "System Notice", 
                JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
        SwingUtilities.invokeLater(() -> {
            
            // 1. Create and show the loading screen
            SplashScreen splash = new SplashScreen();
            
            // 2. Display splash for 1.5 seconds (1500 ms), then launch MainFrame
            splash.showAndDismiss(2000, () -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });
            
        });
//        
    }
}
