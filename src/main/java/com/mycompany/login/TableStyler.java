package com.mycompany.login;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Applies a consistent, modern look to any JTable: no harsh grid lines,
 * a theme-colored header, alternating row stripes derived from the
 * current theme, taller rows, and a themed selection color. Call
 * TableStyler.style(table) once after creating/configuring the table --
 * and again from your screen's refreshTheme() so it stays in sync when
 * the theme changes.
 */
public class TableStyler {

    public static void style(JTable table) {
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(withAlpha(ThemeManager.accent(), 60));
        table.setSelectionForeground(ThemeManager.textPrimary());
        table.setBackground(ThemeManager.cardBackground());
        table.setForeground(ThemeManager.textPrimary());

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(ThemeManager.accent());
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());

        table.setDefaultRenderer(Object.class, new StripedCellRenderer());
    }

    private static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** Flat header cell -- no default 3D bevel, just the theme accent color with white bold text. */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            setBackground(ThemeManager.accent());
            setForeground(Color.WHITE);
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }

    /** Alternating row stripes derived from the theme's card/page colors, with themed selection highlight. */
    private static class StripedCellRenderer extends DefaultTableCellRenderer {
        StripedCellRenderer() {
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                boolean even = row % 2 == 0;
                setBackground(even ? ThemeManager.cardBackground() : stripeShade());
                setForeground(ThemeManager.textPrimary());
            }
            return this;
        }

        private Color stripeShade() {
            Color base = ThemeManager.pageBackground();
            return ThemeManager.isDark()
                    ? new Color(
                        Math.min(255, base.getRed() + 8),
                        Math.min(255, base.getGreen() + 8),
                        Math.min(255, base.getBlue() + 8))
                    : base;
        }
    }
}