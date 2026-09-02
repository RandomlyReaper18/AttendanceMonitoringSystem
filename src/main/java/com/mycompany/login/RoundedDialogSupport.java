package com.mycompany.login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Turns a plain JDialog into a rounded-corner window: undecorated (no
 * native OS title bar), a real clipped rounded shape (not just a rounded
 * rectangle drawn inside a square window -- the corners are genuinely
 * transparent), a themed rounded card background, and a small custom
 * header bar (title + close button) since undecorated windows have
 * nothing to drag by or close with otherwise.
 *
 * Usage: build your dialog's content panel as normal, then call
 * RoundedDialogSupport.apply(dialog, contentPanel, "Title") instead of
 * dialog.setContentPane(contentPanel). Call dialog.pack() or setSize(...)
 * as usual afterward -- the rounded shape is kept in sync automatically.
 */
public class RoundedDialogSupport {

    private static final int ARC = 22;
    private static final int HEADER_HEIGHT = 38;

    public static void apply(JDialog dialog, JComponent innerContent, String title) {
        dialog.setUndecorated(true);
        dialog.getRootPane().setOpaque(false);

        RoundedShell shell = new RoundedShell();
        shell.setLayout(new BorderLayout());

        JPanel header = buildHeader(dialog, title);
        shell.add(header, BorderLayout.NORTH);
        shell.add(innerContent, BorderLayout.CENTER);

        dialog.setContentPane(shell);

        // Keep the window's actual clip shape (and drop shadow illusion via
        // the border) matched to the panel's current size, including after
        // pack()/setSize() are called by the caller.
        shell.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                applyShape(dialog, shell.getWidth(), shell.getHeight());
            }
        });

        dialog.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                applyShape(dialog, dialog.getWidth(), dialog.getHeight());
            }
        });
    }

    private static void applyShape(Window window, int width, int height) {
        if (width > 0 && height > 0) {
            window.setShape(new RoundRectangle2D.Double(0, 0, width, height, ARC, ARC));
        }
    }

    private static JPanel buildHeader(JDialog dialog, String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(10, HEADER_HEIGHT));
        header.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(ThemeManager.textPrimary());

        JButton closeButton = new JButton("\u2715");
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setForeground(ThemeManager.textSecondary());
        closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD, 13f));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dialog.dispose());

        header.add(titleLabel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        // Undecorated windows can't be dragged by the OS -- drag by the header instead.
        DragSupport drag = new DragSupport(dialog);
        header.addMouseListener(drag);
        header.addMouseMotionListener(drag);

        return header;
    }

    /** Rounded card background painted behind the header + content, in the current theme's card color. */
    private static class RoundedShell extends JPanel {
        RoundedShell() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ThemeManager.cardBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2.setColor(ThemeManager.cardBorder());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class DragSupport extends MouseAdapter {
        private final Window window;
        private Point dragStart;

        DragSupport(Window window) {
            this.window = window;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragStart == null) {
                return;
            }
            Point current = e.getLocationOnScreen();
            window.setLocation(current.x - dragStart.x, current.y - dragStart.y);
        }
    }
}