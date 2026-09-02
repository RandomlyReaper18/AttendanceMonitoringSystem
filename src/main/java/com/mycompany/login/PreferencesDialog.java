package com.mycompany.login;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PreferencesDialog extends JDialog {

    private final JLabel appearanceTitle = new JLabel("Appearance");
    private final JLabel appearanceHint = new JLabel("Applies to every screen for everyone using this app.");
    private final JRadioButton lightOption = new JRadioButton("Light Purple");
    private final JRadioButton darkOption = new JRadioButton("Dark Mode");

    private final JLabel accentTitle = new JLabel("Accent Color");
    private final JLabel accentHint = new JLabel("Overrides the theme's default purple everywhere.");
    private final JPanel accentSwatch = new JPanel();
    private final JButton pickColorButton = new RoundedButton("Choose Color...");
    private final JButton resetColorButton = new RoundedButton("Use Default");

    private JLabel bgTitle;
    private JLabel bgHint;
    private JButton chooseBgButton;
    private JButton resetBgButton;

    private final JButton closeButton = new RoundedButton("Done");

    private final Runnable themeListener = this::refreshColors;

    public PreferencesDialog(Window owner, boolean showBackgroundSection) {
        super(owner, "Preferences", ModalityType.APPLICATION_MODAL);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(14, 20, 16, 20));
        content.setOpaque(false);

        appearanceTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appearanceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        appearanceHint.setFont(appearanceHint.getFont().deriveFont(11f));
        appearanceHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        lightOption.setSelected(ThemeManager.getMode() == ThemeManager.Mode.LIGHT_PURPLE);
        darkOption.setSelected(ThemeManager.getMode() == ThemeManager.Mode.DARK);
        styleRadio(lightOption);
        styleRadio(darkOption);

        ButtonGroup group = new ButtonGroup();
        group.add(lightOption);
        group.add(darkOption);

        lightOption.addActionListener(e -> ThemeManager.setMode(ThemeManager.Mode.LIGHT_PURPLE));
        darkOption.addActionListener(e -> ThemeManager.setMode(ThemeManager.Mode.DARK));

        content.add(appearanceTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(appearanceHint);
        content.add(Box.createVerticalStrut(10));
        content.add(lightOption);
        content.add(darkOption);

        // ---- Accent color picker ----
        content.add(Box.createVerticalStrut(18));

        accentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        accentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        accentHint.setFont(accentHint.getFont().deriveFont(11f));
        accentHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(accentTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(accentHint);
        content.add(Box.createVerticalStrut(10));

        accentSwatch.setPreferredSize(new Dimension(28, 28));
        accentSwatch.setMaximumSize(new Dimension(28, 28));
        accentSwatch.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        pickColorButton.addActionListener(e -> pickAccentColor());
        resetColorButton.addActionListener(e -> ThemeManager.clearCustomAccent());

        JPanel accentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        accentRow.setOpaque(false);
        accentRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        accentRow.add(accentSwatch);
        accentRow.add(pickColorButton);
        accentRow.add(resetColorButton);
        content.add(accentRow);

        // ---- Background picture ----
        if (showBackgroundSection) {
            content.add(Box.createVerticalStrut(18));

            bgTitle = new JLabel("Background Picture");
            bgTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            bgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(bgTitle);
            content.add(Box.createVerticalStrut(6));

            bgHint = new JLabel("Shown behind the Login screen.");
            bgHint.setFont(bgHint.getFont().deriveFont(11f));
            bgHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(bgHint);
            content.add(Box.createVerticalStrut(10));

            chooseBgButton = new RoundedButton("Choose Image...");
            chooseBgButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            chooseBgButton.addActionListener(e -> chooseBackgroundImage());

            resetBgButton = new RoundedButton("Reset to Default");
            resetBgButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            resetBgButton.addActionListener(e -> ThemeManager.clearCustomBackground());

            JPanel bgButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bgButtons.setOpaque(false);
            bgButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
            bgButtons.add(chooseBgButton);
            bgButtons.add(resetBgButton);
            content.add(bgButtons);
        }

        content.add(Box.createVerticalStrut(18));

        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.add(closeButton);
        content.add(bottomRow);

        RoundedDialogSupport.apply(this, content, "Preferences");
        setSize(400, showBackgroundSection ? 560 : 400);
        setLocationRelativeTo(owner);

        // Live-refresh every color on screen whenever the theme changes --
        // previously colors were only set once at construction time, so
        // switching modes while this dialog was open left old-theme text
        // sitting on a newly repainted background (e.g. dark text on a
        // background that had just turned dark, making it unreadable).
        ThemeManager.addListener(themeListener);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                ThemeManager.removeListener(themeListener);
            }
        });

        refreshColors();
    }

    @Override
    public void dispose() {
        ThemeManager.removeListener(themeListener);
        super.dispose();
    }

    private void refreshColors() {
        appearanceTitle.setForeground(ThemeManager.textPrimary());
        appearanceHint.setForeground(ThemeManager.textSecondary());
        lightOption.setForeground(ThemeManager.textPrimary());
        darkOption.setForeground(ThemeManager.textPrimary());

        accentTitle.setForeground(ThemeManager.textPrimary());
        accentHint.setForeground(ThemeManager.textSecondary());
        accentSwatch.setBackground(ThemeManager.accent());
        pickColorButton.setBackground(ThemeManager.cardBorder());
        pickColorButton.setForeground(ThemeManager.textPrimary());
        resetColorButton.setBackground(ThemeManager.cardBorder());
        resetColorButton.setForeground(ThemeManager.textPrimary());

        if (bgTitle != null) {
            bgTitle.setForeground(ThemeManager.textPrimary());
            bgHint.setForeground(ThemeManager.textSecondary());
            chooseBgButton.setBackground(ThemeManager.cardBorder());
            chooseBgButton.setForeground(ThemeManager.textPrimary());
            resetBgButton.setBackground(ThemeManager.cardBorder());
            resetBgButton.setForeground(ThemeManager.textPrimary());
        }

        closeButton.setBackground(ThemeManager.accent());
        closeButton.setForeground(Color.WHITE);

        repaint();
    }

    private void styleRadio(JRadioButton radio) {
        radio.setOpaque(false);
        radio.setAlignmentX(Component.LEFT_ALIGNMENT);
        radio.setFocusPainted(false);
    }

    private void pickAccentColor() {
        Color chosen = JColorChooser.showDialog(this, "Choose Accent Color", ThemeManager.accent());
        if (chosen != null) {
            ThemeManager.setCustomAccent(chosen);
        }
    }

    private void chooseBackgroundImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a Background Picture");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image files (jpg, jpeg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        try {
            ThemeManager.setCustomBackground(Path.of(selected.getAbsolutePath()));
            JOptionPane.showMessageDialog(this, "Background picture updated.");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to set background picture: " + e.getMessage());
        }
    }
}