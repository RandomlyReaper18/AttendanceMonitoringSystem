/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.login;

import javax.swing.*;
import java.awt.*;
public class SplashScreen extends JWindow {
    public SplashScreen() {
        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(56, 103, 214), 2),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("ATTENDANCE MONITORING SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Calibri", Font.BOLD, 18));
        title.setForeground(new Color(40, 40, 40));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(new Color(56, 103, 214));

        JLabel status = new JLabel("Loading system data...", SwingConstants.CENTER);
        status.setFont(new Font("Tahoma", Font.PLAIN, 12));

        JPanel inner = new JPanel(new GridLayout(2, 1, 0, 5));
        inner.setOpaque(false);
        inner.add(progressBar);
        inner.add(status);

        content.add(title, BorderLayout.NORTH);
        content.add(inner, BorderLayout.CENTER);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null); // Center on screen
    }

    public void showAndDismiss(int durationMillis, Runnable onComplete) {
        setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(durationMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(() -> {
                dispose();
                onComplete.run();
            });
        }).start();
    }
}