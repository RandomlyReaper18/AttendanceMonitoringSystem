package com.mycompany.login;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Lets the user switch between Light Purple and Dark mode. When opened
 * from the Admin screen (showBackgroundSection = true), it also offers
 * choosing or resetting a custom background picture -- students never
 * see that section, matching "only the admin can set the background".
 *
 * Theme changes apply immediately (every open screen listens to
 * ThemeManager and repaints itself), so there's no separate Apply step.
 */
public class ThemeSettingsDialog extends JDialog {

    public ThemeSettingsDialog(Window owner, boolean showBackgroundSection) {
        super(owner, "Theme Settings", ModalityType.APPLICATION_MODAL);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));
        content.setBackground(ThemeManager.cardBackground());
        setContentPane(content);

        JLabel title = new JLabel("Theme");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(ThemeManager.textPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JRadioButton lightOption = new JRadioButton("Light Purple", ThemeManager.getMode() == ThemeManager.Mode.LIGHT_PURPLE);
        JRadioButton darkOption = new JRadioButton("Dark Mode", ThemeManager.getMode() == ThemeManager.Mode.DARK);
        styleRadio(lightOption);
        styleRadio(darkOption);

        ButtonGroup group = new ButtonGroup();
        group.add(lightOption);
        group.add(darkOption);

        lightOption.addActionListener(e -> ThemeManager.setMode(ThemeManager.Mode.LIGHT_PURPLE));
        darkOption.addActionListener(e -> ThemeManager.setMode(ThemeManager.Mode.DARK));

        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(lightOption);
        content.add(darkOption);

        if (showBackgroundSection) {
            content.add(Box.createVerticalStrut(18));

            JLabel bgTitle = new JLabel("Background Picture");
            bgTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            bgTitle.setForeground(ThemeManager.textPrimary());
            bgTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(bgTitle);
            content.add(Box.createVerticalStrut(6));

            JLabel hint = new JLabel("Shown behind the Login screen.");
            hint.setForeground(ThemeManager.textSecondary());
            hint.setFont(hint.getFont().deriveFont(11f));
            hint.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(hint);
            content.add(Box.createVerticalStrut(10));

            JButton chooseButton = new JButton("Choose Image...");
            chooseButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            chooseButton.addActionListener(e -> chooseBackgroundImage());

            JButton resetButton = new JButton("Reset to Default");
            resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            resetButton.addActionListener(e -> ThemeManager.clearCustomBackground());

            JPanel bgButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bgButtons.setOpaque(false);
            bgButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
            bgButtons.add(chooseButton);
            bgButtons.add(resetButton);
            content.add(bgButtons);
        }

        content.add(Box.createVerticalStrut(18));

        JButton closeButton = new JButton("Done");
        closeButton.setBackground(ThemeManager.accent());
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.add(closeButton);
        content.add(bottomRow);

        setSize(360, showBackgroundSection ? 380 : 230);
        setLocationRelativeTo(owner);
    }

    private void styleRadio(JRadioButton radio) {
        radio.setOpaque(false);
        radio.setForeground(ThemeManager.textPrimary());
        radio.setAlignmentX(Component.LEFT_ALIGNMENT);
        radio.setFocusPainted(false);
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