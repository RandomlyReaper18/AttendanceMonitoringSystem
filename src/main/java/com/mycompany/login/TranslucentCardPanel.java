package com.mycompany.login;

import javax.swing.*;
import java.awt.*;

/**
 * A panel with a semi-transparent rounded-rectangle background,
 * meant to sit on top of a BackgroundPanel image so text/fields
 * placed on it stay readable regardless of what's behind them.
 */
public class TranslucentCardPanel extends JPanel {

    private Color fillColor = new Color(255, 255, 255, 180); // white, ~70% opaque
    private int arc = 20; // corner rounding

    public TranslucentCardPanel() {
        setOpaque(false); // let paintComponent handle the fill so corners stay rounded
    }

    public void setFillColor(Color c) {
        this.fillColor = c;
        repaint();
    }

    public void setCornerArc(int arc) {
        this.arc = arc;
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