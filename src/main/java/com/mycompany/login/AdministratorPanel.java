package com.mycompany.login;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.table.DefaultTableModel;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * The admin dashboard screen. Has three tabs: "Attendance", "Manage Users",
 * and "Reason Log".
 */
public class AdministratorPanel extends JPanel {

    private static final Color PAGE_BG = new Color(244, 246, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(224, 227, 233);

    private final MainFrame mainFrame;

    private JButton jButton1;
    private JButton jButton2;
    private JButton jButton3;
    private JButton jButton4;
    private JButton addStudentsButton;
    private JButton changeAdminButton;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel6;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JTable jTable1;
    private JTextField jTextField1;

    private JPanel statsRow;
    private StatMiniCard attendanceCard;
    private StatMiniCard presentCard;
    private StatMiniCard absentCard;
    private StatMiniCard lateCard;
    private StatMiniCard registeredCard;

    private JTabbedPane tabbedPane;
    private JComboBox<String> sectionComboBox;
    private JTable usersTable;
    private JButton deleteUserButton;
    private JTable reasonLogTable;

    public AdministratorPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int size = Math.max(18, getWidth() / 35);
                jLabel1.setFont(new Font("Tahoma", Font.BOLD, size));
            }
        });

        jTable1.setFillsViewportHeight(true);
        jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
            "Username", "Name", "Date", "Login Time", "Logout Time", "Status", "Attendance"
        });
        jTable1.setModel(model);

        usersTable.setFillsViewportHeight(true);
        usersTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        DefaultTableModel usersModel = new DefaultTableModel();
        usersModel.setColumnIdentifiers(new String[]{"Username", "Full Name"});
        usersTable.setModel(usersModel);

        loadTodayAttendance();
        loadUsersTable();
        loadReasonLogTable();
        loadSectionComboBox();
        updateStatistics();
        setupUsersContextMenu();

        Timer clock = new Timer(1000, e ->
                jLabel4.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"))));
        clock.start();

        Timer refreshTimer = new Timer(1000, e -> {
            if (jTable1.getSelectedRow() == -1) {
                loadTodayAttendance();
            }
        });
        refreshTimer.start();

        jLabel3.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchUser();
            }
        });
    }

    /** Called by MainFrame right before this card becomes visible. */
    public void onShow() {
        loadTodayAttendance();
        loadUsersTable();
        loadReasonLogTable();
        loadSectionComboBox();
        updateStatistics();
    }

    private void updateStatistics() {
        ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();
        ArrayList<User> users = UserManager.loadUsers();

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        int total = 0;
        int present = 0;
        int late = 0;

        for (Attendance a : attendance) {
            if (a.getDate().equals(today)) {
                total++;
                if ("Present".equals(a.getAttendanceStatus())) {
                    present++;
                }
                if ("Late".equals(a.getAttendanceStatus())) {
                    late++;
                }
            }
        }

        int registered = users.size();
        int absent = Math.max(0, registered - total);

        attendanceCard.setValue(String.valueOf(total));
        presentCard.setValue(String.valueOf(present));
        absentCard.setValue(String.valueOf(absent));
        lateCard.setValue(String.valueOf(late));
        registeredCard.setValue(String.valueOf(registered));
    }

    private void loadTodayAttendance() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();

        for (Attendance a : attendance) {
            if (a.getDate().equals(today)) {
                model.addRow(new Object[]{
                    a.getUsername(), a.getName(), a.getDate(), a.getLoginTime(),
                    a.getLogoutTime(), a.getStatus(), a.getAttendanceStatus()
                });
            }
        }
        jLabel2.setText("Logged In Users : " + jTable1.getRowCount());
    }

    private void loadUsersTable() {
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        model.setRowCount(0);

        for (User u : UserManager.loadUsers()) {
            model.addRow(new Object[]{u.getUsername(), u.getName()});
        }
    }

    private void loadSectionComboBox() {
        String previouslySelected = (String) sectionComboBox.getSelectedItem();
        sectionComboBox.removeAllItems();
        for (String section : StudentSectionManager.getAllSectionNames()) {
            sectionComboBox.addItem(section);
        }
        if (previouslySelected != null) {
            sectionComboBox.setSelectedItem(previouslySelected);
        }
    }

    /** Force-logs-out every currently logged-in student in the selected section. */
    private void logOutSelectedSection() {
        String section = (String) sectionComboBox.getSelectedItem();
        if (section == null) {
            JOptionPane.showMessageDialog(this, "No sections found yet \u2014 add students with a Section first.");
            return;
        }

        ArrayList<String> usernames = StudentSectionManager.getUsernamesInSection(section);
        if (usernames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students found in \"" + section + "\".");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Force log out all students in \"" + section + "\" (" + usernames.size() + " student(s))?",
                "Confirm Section Logout", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();
        int loggedOut = 0;
        for (String username : usernames) {
            boolean removed = attendance.removeIf(a ->
                    a.getUsername().equals(username) && a.getStatus().equals("Logged In"));
            if (removed) {
                loggedOut++;
            }
        }

        AttendanceManager.saveAttendance(attendance);
        loadTodayAttendance();
        updateStatistics();
        JOptionPane.showMessageDialog(this, loggedOut + " student(s) in \"" + section + "\" logged out.");
    }

    private void loadReasonLogTable() {
        DefaultTableModel model = (DefaultTableModel) reasonLogTable.getModel();
        model.setRowCount(0);

        ArrayList<LogEntry> entries = ReasonLogManager.loadEntries();
        for (int i = entries.size() - 1; i >= 0; i--) {
            LogEntry e = entries.get(i);
            model.addRow(new Object[]{
                e.getUsername(), e.getFullName(), e.getDate(),
                "LATE".equals(e.getType()) ? "Late" : "Absence",
                e.getAnswer(), e.getLoggedAt()
            });
        }
    }

    private void changeAdminCredentials() {
        UserManager.AdminAccount currentAdmin = UserManager.loadAdmin();
        
        JTextField usernameField = new JTextField(currentAdmin.getUsername());
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        passwordField.setEchoChar('\u2022');
        confirmPasswordField.setEchoChar('\u2022');

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.addActionListener(e -> {
            char echo = showPassword.isSelected() ? (char) 0 : '\u2022';
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(new JLabel("New Admin Username:"));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("New Admin Password:"));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Confirm New Password:"));
        form.add(confirmPasswordField);
        form.add(Box.createVerticalStrut(4));
        form.add(showPassword);

        int result = JOptionPane.showConfirmDialog(this, form, "Change Admin Credentials",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String newUsername = usernameField.getText().trim();
        String newPassword = String.valueOf(passwordField.getPassword()).trim();
        String confirmPassword = String.valueOf(confirmPasswordField.getPassword()).trim();

        if (newUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        if (!newPassword.isEmpty() && !UserManager.isStrongPassword(newPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Password must contain:\n\n"
                    + "- Minimum 8 characters\n"
                    + "- At least 1 uppercase letter\n"
                    + "- At least 1 lowercase letter\n"
                    + "- At least 1 number\n"
                    + "- At least 1 special character");
            return;
        }

        boolean updated = UserManager.updateAdminCredentials(newUsername, newPassword);

        if (updated) {
            JOptionPane.showMessageDialog(this, "Admin credentials updated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update admin credentials.");
        }
    }

    private void setupUsersContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem resetItem = new JMenuItem("Reset Password");
        resetItem.addActionListener(e -> resetSelectedUserPassword());

        JMenuItem qrItem = new JMenuItem("View QR Code");
        qrItem.addActionListener(e -> viewOrGenerateQrCode());

        JMenuItem calendarItem = new JMenuItem("View Attendance Calendar");
        calendarItem.addActionListener(e -> openAttendanceCalendar());

        JMenuItem editItem = new JMenuItem("Edit User");
        editItem.addActionListener(e -> editSelectedUser());

        JMenuItem deleteItem = new JMenuItem("Delete User");
        deleteItem.addActionListener(e -> deleteSelectedUser());

        menu.add(resetItem);
        menu.add(qrItem);
        menu.add(calendarItem);
        menu.add(editItem);
        menu.addSeparator();
        menu.add(deleteItem);

        usersTable.setComponentPopupMenu(menu);

        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                selectRowUnderCursor(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                selectRowUnderCursor(e);
            }

            private void selectRowUnderCursor(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = usersTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        usersTable.setRowSelectionInterval(row, row);
                    }
                }
            }
        });
    }

    private User findUserByUsername(String username) {
        for (User u : UserManager.loadUsers()) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    private void openAttendanceCalendar() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        String username = usersTable.getValueAt(row, 0).toString();
        String name = usersTable.getValueAt(row, 1).toString();

        Window owner = SwingUtilities.getWindowAncestor(this);
        AttendanceCalendarDialog dialog = new AttendanceCalendarDialog(owner, username, name);
        dialog.setVisible(true);
    }

    private void resetSelectedUserPassword() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        String username = usersTable.getValueAt(row, 0).toString();
        User target = findUserByUsername(username);

        if (target == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Generate a new password for \"" + username + "\"?\n"
                + "Their current password will stop working immediately.",
                "Reset Password", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String newPassword = generateStrongPassword();
        boolean updated = UserManager.updateUser(username, username, newPassword, target.getName());

        if (!updated) {
            JOptionPane.showMessageDialog(this, "Failed to reset password.");
            return;
        }

        JTextField passwordField = new JTextField(newPassword);
        passwordField.setEditable(false);
        passwordField.setFont(passwordField.getFont().deriveFont(Font.BOLD, 16f));

        JButton copyBtn = new JButton("Copy");
        copyBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(newPassword);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        });

        JPanel row2 = new JPanel(new BorderLayout(6, 0));
        row2.add(passwordField, BorderLayout.CENTER);
        row2.add(copyBtn, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("New password for " + username + " \u2014 write this down now, it can't be shown again:"), BorderLayout.NORTH);
        panel.add(row2, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, "New Password", JOptionPane.INFORMATION_MESSAGE);
    }

    private String generateStrongPassword() {
        String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String lower = "abcdefghijkmnpqrstuvwxyz";
        String digits = "23456789";
        String special = "!@#$%*";
        String all = upper + lower + digits + special;

        java.util.Random rnd = new java.util.Random();
        char[] result = new char[10];
        result[0] = upper.charAt(rnd.nextInt(upper.length()));
        result[1] = lower.charAt(rnd.nextInt(lower.length()));
        result[2] = digits.charAt(rnd.nextInt(digits.length()));
        result[3] = special.charAt(rnd.nextInt(special.length()));
        for (int i = 4; i < result.length; i++) {
            result[i] = all.charAt(rnd.nextInt(all.length()));
        }

        for (int i = result.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            char tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }
        return new String(result);
    }

    private void editSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        String oldUsername = usersTable.getValueAt(row, 0).toString();
        User target = findUserByUsername(oldUsername);

        if (target == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
            return;
        }

        JTextField usernameField = new JTextField(target.getUsername());
        JTextField nameField = new JTextField(target.getName());
        JPasswordField passwordField = new JPasswordField();
        passwordField.setEchoChar('\u2022');

        JLabel passwordHint = new JLabel("Leave blank to keep the current password.");
        passwordHint.setForeground(new Color(120, 120, 120));
        passwordHint.setFont(passwordHint.getFont().deriveFont(11f));

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.addActionListener(e ->
                passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '\u2022'));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("New Password (optional):"));
        form.add(passwordField);
        form.add(passwordHint);
        form.add(showPassword);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String newUsername = usernameField.getText().trim();
        String newName = nameField.getText().trim();
        String newPassword = String.valueOf(passwordField.getPassword()).trim();

        if (newUsername.isEmpty() || newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Full Name are required.");
            return;
        }

        if (!newPassword.isEmpty() && !UserManager.isStrongPassword(newPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Password must contain:\n\n"
                    + "- Minimum 8 characters\n"
                    + "- At least 1 uppercase letter\n"
                    + "- At least 1 lowercase letter\n"
                    + "- At least 1 number\n"
                    + "- At least 1 special character\n\n"
                    + "Or leave it blank to keep the current password.");
            return;
        }

        boolean updated = UserManager.updateUser(oldUsername, newUsername, newPassword, newName);

        if (updated) {
            loadUsersTable();
            updateStatistics();
            JOptionPane.showMessageDialog(this, "User updated.");
        } else {
            JOptionPane.showMessageDialog(this, "Update failed \u2014 that username may already be taken.");
        }
    }

    private void viewOrGenerateQrCode() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        String username = usersTable.getValueAt(row, 0).toString();

        if (QrCodeGenerator.hasQrCode(username)) {
            showQrDialog(username, QrCodeGenerator.getQrPath(username));
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "No QR code exists yet for \"" + username + "\" (passwords are hashed, so one can't be "
                + "generated from an existing password). Reset their password and generate a new QR code now?",
                "No QR Code Yet", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        User target = findUserByUsername(username);
        if (target == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
            return;
        }

        String newPassword = generateStrongPassword();
        boolean updated = UserManager.updateUser(username, username, newPassword, target.getName());
        if (!updated) {
            JOptionPane.showMessageDialog(this, "Failed to reset password.");
            return;
        }

        try {
            Path qrPath = QrCodeGenerator.saveForStudent(username, newPassword);
            JOptionPane.showMessageDialog(this,
                    "New password for " + username + ": " + newPassword
                    + "\n(write this down now, it can't be shown again)",
                    "New Password", JOptionPane.INFORMATION_MESSAGE);
            showQrDialog(username, qrPath);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Password was reset, but the QR code failed to save: " + e.getMessage());
        }
    }

    private void showQrDialog(String username, Path qrPath) {
        try {
            BufferedImage img = ImageIO.read(qrPath.toFile());
            JLabel imgLabel = new JLabel(new ImageIcon(img));

            JButton openFolder = new JButton("Open Folder");
            openFolder.addActionListener(e -> {
                try {
                    Desktop.getDesktop().open(qrPath.getParent().toFile());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonRow.add(openFolder);

            JPanel content = new JPanel(new BorderLayout(0, 10));
            content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            content.add(imgLabel, BorderLayout.CENTER);
            content.add(buttonRow, BorderLayout.SOUTH);

            JDialog dialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), "QR Code \u2014 " + username, true);
            dialog.setContentPane(content);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load QR code image: " + e.getMessage());
        }
    }

    private void deleteSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.");
            return;
        }

        String username = usersTable.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user \"" + username + "\"? This cannot be undone.\n"
                + "(Their past attendance records will be kept.)",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean removed = UserManager.deleteUser(username);

        if (removed) {
            loadUsersTable();
            updateStatistics();
            JOptionPane.showMessageDialog(this, "User \"" + username + "\" deleted.");
        } else {
            JOptionPane.showMessageDialog(this, "User not found.");
        }
    }

    private void initComponents() {
        jScrollPane1 = new JScrollPane();
        jTable1 = new JTable();
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();
        jButton2 = new JButton();
        jButton3 = new JButton();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jButton4 = new JButton();
        jPanel2 = new JPanel();
        jLabel6 = new JLabel();
        jTextField1 = new JTextField();
        jButton1 = new JButton();

        setBackground(PAGE_BG);
        setOpaque(true);

        jTable1.setModel(new DefaultTableModel(
            new Object[][]{{null, null, null, null}, {null, null, null, null}, {null, null, null, null}, {null, null, null, null}},
            new String[]{"Title 1", "Title 2", "Title 3", "Title 4"}
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.setBackground(new Color(51, 153, 255));

        jLabel1.setFont(new Font("Tahoma", Font.BOLD, 24));
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText("ADMINISTRATOR");

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jButton2.setText("Log Out User");
        jButton2.setBackground(new Color(200, 55, 55));
        jButton2.setForeground(Color.WHITE);
        jButton2.setFocusPainted(false);
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setText("Refresh");
        jButton3.setBackground(new Color(56, 103, 214));
        jButton3.setForeground(Color.WHITE);
        jButton3.setFocusPainted(false);
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setText("Login");
        jButton4.setFocusPainted(false);
        jButton4.addActionListener(this::jButton4ActionPerformed);

        addStudentsButton = new JButton("Add Students");
        addStudentsButton.setBackground(new Color(46, 160, 67));
        addStudentsButton.setForeground(Color.WHITE);
        addStudentsButton.setFocusPainted(false);
        addStudentsButton.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_STUDENT_FORM));

        changeAdminButton = new JButton("Change Admin Password");
        changeAdminButton.setBackground(new Color(100, 110, 120));
        changeAdminButton.setForeground(Color.WHITE);
        changeAdminButton.setFocusPainted(false);
        changeAdminButton.addActionListener(e -> changeAdminCredentials());

        jLabel2.setFont(new Font("Tahoma", Font.BOLD, 13));
        jLabel2.setHorizontalAlignment(SwingConstants.LEFT);
        jLabel2.setText("Logged In Users : 0");

        jLabel3.setFont(new Font("Tahoma", Font.BOLD, 13));
        jLabel3.setHorizontalAlignment(SwingConstants.LEFT);
        jLabel3.setText(".");

        jLabel4.setFont(new Font("Tahoma", Font.BOLD, 13));
        jLabel4.setHorizontalAlignment(SwingConstants.LEFT);
        jLabel4.setText("");

        jLabel6.setFont(new Font("Segoe UI", Font.BOLD, 16));
        jLabel6.setForeground(new Color(40, 40, 40));
        jLabel6.setText("DASHBOARD");

        jTextField1.addActionListener(this::jTextField1ActionPerformed);
        jTextField1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        jButton1.setText("Search");
        jButton1.setBackground(new Color(56, 103, 214));
        jButton1.setForeground(Color.WHITE);
        jButton1.setFocusPainted(false);
        jButton1.addActionListener(this::jButton1ActionPerformed);

        attendanceCard = new StatMiniCard("TODAY'S ATTENDANCE", new Color(56, 103, 214));
        presentCard = new StatMiniCard("PRESENT", new Color(46, 160, 67));
        absentCard = new StatMiniCard("ABSENT", new Color(200, 55, 55));
        lateCard = new StatMiniCard("LATE", new Color(230, 140, 30));
        registeredCard = new StatMiniCard("REGISTERED USERS", new Color(0, 140, 140));

        statsRow = new JPanel(new GridLayout(1, 5, 12, 0));
        statsRow.setOpaque(false);
        statsRow.add(attendanceCard);
        statsRow.add(presentCard);
        statsRow.add(absentCard);
        statsRow.add(lateCard);
        statsRow.add(registeredCard);

        jPanel2.setBackground(CARD_BG);
        jPanel2.setOpaque(true);
        jPanel2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6)
            .addComponent(statsRow, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jTextField1)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(12, 12, 12)
                .addComponent(statsRow, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)))
        );

        tabbedPane = new JTabbedPane();

        sectionComboBox = new JComboBox<>();
        JButton logOutSectionButton = new JButton("Log Out Section");
        logOutSectionButton.setBackground(new Color(200, 55, 55));
        logOutSectionButton.setForeground(Color.WHITE);
        logOutSectionButton.setFocusPainted(false);
        logOutSectionButton.addActionListener(e -> logOutSelectedSection());

        JPanel sectionLogoutRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        sectionLogoutRow.setOpaque(false);
        sectionLogoutRow.add(new JLabel("Section:"));
        sectionLogoutRow.add(sectionComboBox);
        sectionLogoutRow.add(logOutSectionButton);

        JPanel attendanceTab = new JPanel(new BorderLayout());
        attendanceTab.setOpaque(false);
        attendanceTab.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        attendanceTab.add(sectionLogoutRow, BorderLayout.NORTH);
        attendanceTab.add(jScrollPane1, BorderLayout.CENTER);
        tabbedPane.addTab("Attendance", attendanceTab);

        usersTable = new JTable();
        JScrollPane usersScroll = new JScrollPane(usersTable);

        deleteUserButton = new JButton("Delete Selected User");
        deleteUserButton.setBackground(new Color(200, 55, 55));
        deleteUserButton.setForeground(Color.WHITE);
        deleteUserButton.setFocusPainted(false);
        deleteUserButton.addActionListener(e -> deleteSelectedUser());

        JPanel usersButtonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        usersButtonRow.setOpaque(false);
        usersButtonRow.add(deleteUserButton);

        JPanel usersTab = new JPanel(new BorderLayout(0, 8));
        usersTab.setOpaque(false);
        usersTab.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        usersTab.add(usersScroll, BorderLayout.CENTER);
        usersTab.add(usersButtonRow, BorderLayout.SOUTH);
        tabbedPane.addTab("Manage Users", usersTab);

        reasonLogTable = new JTable();
        reasonLogTable.setModel(new DefaultTableModel(
                new String[]{"Username", "Name", "Date", "Type", "Answer", "Answered At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });
        JScrollPane reasonLogScroll = new JScrollPane(reasonLogTable);

        JPanel reasonLogTab = new JPanel(new BorderLayout());
        reasonLogTab.setOpaque(false);
        reasonLogTab.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        reasonLogTab.add(reasonLogScroll, BorderLayout.CENTER);
        tabbedPane.addTab("Reason Log", reasonLogTab);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
                                .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                                .addGap(65, 65, 65)
                                .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addGap(18, 18, 18)
                                .addComponent(jButton3)
                                .addGap(26, 26, 26)
                                .addComponent(jButton4)
                                .addGap(26, 26, 26)
                                .addComponent(addStudentsButton)
                                .addGap(26, 26, 26)
                                .addComponent(changeAdminButton)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4)
                    .addComponent(addStudentsButton)
                    .addComponent(changeAdminButton))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jScrollPane1.getAccessibleContext().setAccessibleName("");
    }

    private void searchUser() {
        String keyword = jTextField1.getText().trim().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();

        for (Attendance a : attendance) {
            if (a.getUsername().toLowerCase().contains(keyword)) {
                model.addRow(new Object[]{
                    a.getUsername(), a.getName(), a.getDate(), a.getLoginTime(),
                    a.getLogoutTime(), a.getStatus(), a.getAttendanceStatus()
                });
            }
        }
    }

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] rows = jTable1.getSelectedRows();

        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select at least one user.");
            return;
        }

        if (rows.length > 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Force log out " + rows.length + " selected users?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        Set<String> usernames = new HashSet<>();
        for (int row : rows) {
            usernames.add(jTable1.getValueAt(row, 0).toString());
        }

        ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();
        int loggedOut = 0;
        for (String username : usernames) {
            boolean removed = attendance.removeIf(a ->
                    a.getUsername().equals(username) && a.getStatus().equals("Logged In"));
            if (removed) {
                loggedOut++;
            }
        }

        if (loggedOut > 0) {
            AttendanceManager.saveAttendance(attendance);
            loadTodayAttendance();
            updateStatistics();
            JOptionPane.showMessageDialog(this, loggedOut + " user(s) logged out successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "No matching logged-in users found.");
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String keyword = jTextField1.getText().trim().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();
        ArrayList<User> users = UserManager.loadUsers();

        for (Attendance a : attendance) {
            for (User u : users) {
                if (u.getUsername().equals(a.getUsername())) {
                    if (a.getUsername().toLowerCase().contains(keyword)
                            || u.getName().toLowerCase().contains(keyword)) {

                        model.addRow(new Object[]{
                            a.getUsername(), u.getName(), a.getDate(), a.getLoginTime(),
                            a.getLogoutTime(), a.getStatus(), a.getAttendanceStatus()
                        });
                    }
                }
            }
        }
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        loadTodayAttendance();
        loadUsersTable();
        loadReasonLogTable();
        loadSectionComboBox();
        updateStatistics();
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        mainFrame.showCard(MainFrame.CARD_LOGIN);
    }
}