package com.mycompany.login;

import javax.swing.*;
import java.awt.*;

/**
 * Drop-in replacement for JButton with rounded corners and a subtle
 * hover/press shade, matching the app's rounded design language.
 * Works exactly like a normal JButton -- setBackground(), setForeground(),
 * setFont(), addActionListener(), etc. all behave the same; only the
 * painting is different.
 */
public class RoundedButton extends JButton {

    private int arc = 16;

    public RoundedButton() {
        init();
    }

    public RoundedButton(String text) {
        super(text);
        init();
    }

    private void init() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /** Corner rounding radius in pixels. Default 16. */
    public void setCornerArc(int arc) {
        this.arc = arc;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = getBackground();
        Color fill = base;
        if (!isEnabled()) {
            fill = new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.max(90, base.getAlpha() / 2));
        } else if (getModel().isPressed()) {
            fill = shade(base, -0.12f);
        } else if (getModel().isRollover()) {
            fill = shade(base, 0.10f);
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();

        super.paintComponent(g);
    }

    /** amount > 0 brightens, amount < 0 darkens, preserving alpha. */
    private Color shade(Color c, float amount) {
        int r = clamp((int) (c.getRed() + 255 * amount));
        int g = clamp((int) (c.getGreen() + 255 * amount));
        int b = clamp((int) (c.getBlue() + 255 * amount));
        return new Color(r, g, b, c.getAlpha());
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}