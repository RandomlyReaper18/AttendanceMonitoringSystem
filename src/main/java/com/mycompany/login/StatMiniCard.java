package com.mycompany.login;

import javax.swing.*;
import java.awt.*;

/**
 * A small white rounded card with a colored accent bar on top, a title,
 * and a big bold number underneath -- matches the "Total Students /
 * Present Today / Absent Today / Late Today" style from the reference
 * dashboard screenshot.
 */
public class StatMiniCard extends JPanel {

    private final JLabel valueLabel;
    private final int arc = 14;
    private final Color accentColor;

    public StatMiniCard(String title, Color accentColor) {
        this.accentColor = accentColor;
        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(150, 90));

        JPanel accentBar = new JPanel();
        accentBar.setOpaque(true);
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(10, 4));
        accentBar.setBorder(BorderFactory.createEmptyBorder());

        JPanel accentWrap = new JPanel(new BorderLayout());
        accentWrap.setOpaque(false);
        accentWrap.setBorder(BorderFactory.createEmptyBorder(6, 10, 0, 10));
        accentWrap.add(accentBar, BorderLayout.CENTER);
        add(accentWrap, BorderLayout.NORTH);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(6, 14, 10, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel = new JLabel("0");
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(new Color(35, 35, 35));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);

        add(textPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}