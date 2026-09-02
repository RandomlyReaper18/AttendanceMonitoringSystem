package com.mycompany.login;

import javax.swing.*;
import java.awt.*;

public class TutorialDialog extends JDialog {

    public TutorialDialog(Window owner) {
        super(owner, "Getting Started", ModalityType.APPLICATION_MODAL);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(6, 18, 14, 18));

        JLabel title = new JLabel("Welcome to the Attendance Monitoring System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(ThemeManager.textPrimary());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("For Students", buildScrollableHtml(studentHtml()));
        tabs.addTab("For Admins", buildScrollableHtml(adminHtml()));
        tabs.addTab("QR Codes", buildScrollableHtml(qrHtml()));

        JCheckBox dontShowAgain = new JCheckBox("Don't show this automatically next time");
        dontShowAgain.setOpaque(false);
        dontShowAgain.setForeground(ThemeManager.textSecondary());

        JButton closeButton = new RoundedButton("Got it!");
        closeButton.setBackground(ThemeManager.accent());
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> {
            if (dontShowAgain.isSelected()) {
                markTutorialSeen();
            }
            dispose();
        });

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.add(dontShowAgain, BorderLayout.WEST);

        JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrap.setOpaque(false);
        buttonWrap.add(closeButton);
        bottomRow.add(buttonWrap, BorderLayout.EAST);

        content.add(title, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        content.add(bottomRow, BorderLayout.SOUTH);

        RoundedDialogSupport.apply(this, content, "Getting Started");
        setSize(560, 520);
        setLocationRelativeTo(owner);
    }

    private JScrollPane buildScrollableHtml(String html) {
        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        pane.setBackground(ThemeManager.cardBackground());
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(pane);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private String studentHtml() {
        return "<html><body style='font-family:Segoe UI; font-size:11px; color:#333;'>"
            + "<h3>Logging In</h3>"
            + "<ol>"
            + "<li>Type your <b>Username</b> and <b>Password</b> on the login screen, then click <b>LOGIN</b>.</li>"
            + "<li>Or click <b>SCAN QR CODE</b> and hold your printed/saved QR code up to the camera \u2014 "
            +   "it fills in your username and password and logs you in automatically.</li>"
            + "</ol>"
            + "<h3>Being Marked Late or Absent</h3>"
            + "<ul>"
            + "<li>Logging in after the daily cutoff time marks you <b>Late</b>, and you'll be asked for a quick reason.</li>"
            + "<li>If you missed the previous school day, you may be asked for an excuse letter the next time you log in.</li>"
            + "</ul>"
            + "<h3>Logging Out</h3>"
            + "<p>Select your row in the table and click <b>LOG OUT</b> before you leave.</p>"
            + "</body></html>";
    }

    private String adminHtml() {
        return "<html><body style='font-family:Segoe UI; font-size:11px; color:#333;'>"
            + "<h3>Dashboard</h3>"
            + "<p>The stat cards at the top show today's totals: attendance, present, absent, late, and registered users.</p>"
            + "<h3>Attendance Tab</h3>"
            + "<ul>"
            + "<li>Shows everyone logged in today. Select rows and click <b>Log Out User</b> to force a logout.</li>"
            + "<li>Use the <b>Section</b> dropdown with <b>Log Out Section</b> to log out an entire section/class at once.</li>"
            + "<li><b>Delete Section</b> permanently removes every student account in that section (attendance history is kept).</li>"
            + "</ul>"
            + "<h3>Manage Users Tab</h3>"
            + "<p>Right-click a student to <b>Reset Password</b>, <b>View QR Code</b>, <b>View Attendance Calendar</b>, "
            + "<b>Edit User</b>, or <b>Delete User</b>.</p>"
            + "<h3>Reason Log Tab</h3>"
            + "<p>Shows every reason/excuse students have submitted for being late or absent.</p>"
            + "<h3>Adding Students</h3>"
            + "<p>Click <b>Add Students</b> to paste a class list and auto-generate usernames, passwords, and QR codes "
            + "for a whole section in one go.</p>"
            + "</body></html>";
    }

    private String qrHtml() {
        return "<html><body style='font-family:Segoe UI; font-size:11px; color:#333;'>"
            + "<h3>How QR Login Works</h3>"
            + "<p>Each student's QR code encodes their username and password as plain text \u2014 "
            + "scanning it is the same as typing them in.</p>"
            + "<h3>Where QR Codes Are Saved</h3>"
            + "<p>QR code images are saved as PNG files in the <b>Asys Attendance Records \\ qr_codes</b> folder "
            + "on the Desktop, named after each student's username.</p>"
            + "<h3>Important</h3>"
            + "<p>Because passwords are stored securely (hashed), a QR code can only be generated at the moment "
            + "a password is set \u2014 during account creation or a password reset. If a student loses their QR code, "
            + "an admin needs to reset their password to generate a new one.</p>"
            + "</body></html>";
    }

    /** True the very first time the app runs on this machine (no marker file yet). */
    public static boolean isFirstLaunch() {
        return !java.nio.file.Files.exists(markerFile());
    }

    /** Records that the user has seen (and dismissed with "don't show again") the tutorial. */
    public static void markTutorialSeen() {
        try {
            java.nio.file.Files.createDirectories(markerFile().getParent());
            java.nio.file.Files.writeString(markerFile(), "seen");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static java.nio.file.Path markerFile() {
        return AppPaths.privateDataDir().resolve("tutorial_seen.flag");
    }
}