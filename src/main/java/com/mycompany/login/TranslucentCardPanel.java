package com.mycompany.login;

import javax.swing.*;
import java.awt.*;

/**
 * A panel with a semi-transparent rounded-rectangle background,
 * meant to sit on top of a BackgroundPanel image so text/fields
 * placed on it stay readable regardless of what's behind them.
 */
public class TranslucentCardPanel extends JPanel {

    private Color fillColor;
    private int arc = 20; // corner rounding

    public TranslucentCardPanel() {
        setOpaque(false); // let paintComponent handle the fill so corners stay rounded
        refreshTheme();
    }

    public void setFillColor(Color c) {
        this.fillColor = c;
        repaint();
    }

    public void setCornerArc(int arc) {
        this.arc = arc;
        repaint();
    }

    /** Re-derives the translucent fill from the current theme's card color. */
    public void refreshTheme() {
        Color base = ThemeManager.cardBackground();
        this.fillColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), 210);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}