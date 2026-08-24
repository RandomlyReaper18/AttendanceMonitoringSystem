package com.mycompany.login;

import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

/**
 * The login + live attendance table screen. Table placement now mirrors
 * SignupPanel's (left margin, LOG OUT button beside the table instead of
 * stretched full-width below it), and a row of small stat cards sits
 * underneath, matching the reference dashboard's Total/Present/Absent/Late
 * style.
 */
public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;

    private BackgroundPanel jPanel3;
    private JPanel jPanel4; // TranslucentCardPanel, the login card
    private JButton jButton1; // LOGIN
    private JLabel jLabel3;
    private JLabel jLabel2;
    private JTextField jTextField1;
    private JPasswordField jPasswordField1;
    private JLabel jLabel1;
    private JButton scanQrButton;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JTable jTable1;
    private JButton jButton4; // LOG OUT

    private JPanel statsRow;
    private StatMiniCard totalStudentsCard;
    private StatMiniCard presentCard;
    private StatMiniCard absentCard;
    private StatMiniCard lateCard;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
        // No background photo loaded -- jPanel3 (BackgroundPanel) paints
        // a clean gradient by default when no image is set.

        jTable1.setFillsViewportHeight(true);
        jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
            "Username", "Date", "Login Time", "Logout Time", "Status"
        });
        jTable1.setModel(model);

        jPasswordField1.addActionListener(e -> jButton1.doClick());

        loadTodayAttendance();
        updateStatCards();
    }

    /** Called by MainFrame right before this card becomes visible. */
    public void onShow() {
        jTextField1.setText("");
        jPasswordField1.setText("");
        loadTodayAttendance();
        updateStatCards();
    }

    private void loadTodayAttendance() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();

        for (Attendance a : attendance) {
            if (a.getDate().equals(today)) {
                model.addRow(new Object[]{
                    a.getUsername(), a.getDate(), a.getLoginTime(), a.getLogoutTime(), a.getStatus()
                });
            }
        }
    }

    private void updateStatCards() {
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

        totalStudentsCard.setValue(String.valueOf(registered));
        presentCard.setValue(String.valueOf(present));
        absentCard.setValue(String.valueOf(absent));
        lateCard.setValue(String.valueOf(late));
    }

    private void initComponents() {
        jPanel3 = new BackgroundPanel();
        jPanel4 = new TranslucentCardPanel();
        jButton1 = new JButton();
        jLabel3 = new JLabel();
        jLabel2 = new JLabel();
        jTextField1 = new JTextField();
        jPasswordField1 = new JPasswordField();
        jLabel1 = new JLabel();
        jPanel1 = new JPanel();
        jScrollPane1 = new JScrollPane();
        jTable1 = new JTable();
        jButton4 = new JButton();

        jButton1.setText("LOGIN");
        jButton1.setBackground(new Color(56, 103, 214));
        jButton1.setForeground(Color.WHITE);
        jButton1.setFocusPainted(false);
        jButton1.setFont(jButton1.getFont().deriveFont(Font.BOLD));
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jLabel3.setFont(new Font("Tahoma", Font.BOLD, 10));
        jLabel3.setText("Password: ");

        jLabel2.setFont(new Font("Tahoma", Font.BOLD, 10));
        jLabel2.setText("Username: ");

        jLabel1.setFont(new Font("Calibri", Font.BOLD, 24));
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText("ATTENDANCE MONITORING SYSTEM");

        scanQrButton = new JButton("SCAN QR CODE");
        scanQrButton.setBackground(new Color(46, 160, 67));
        scanQrButton.setForeground(Color.WHITE);
        scanQrButton.setFocusPainted(false);
        scanQrButton.setFont(scanQrButton.getFont().deriveFont(Font.BOLD));
        scanQrButton.addActionListener(e -> openQrScanner());

        jTextField1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 227, 233), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        jPasswordField1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 227, 233), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jPasswordField1)
                                .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 251, GroupLayout.PREFERRED_SIZE))
                            .addGroup(GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 64, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29))
                            .addComponent(scanQrButton, GroupLayout.PREFERRED_SIZE, 251, GroupLayout.PREFERRED_SIZE))))
                .addGap(61, 61, 61))
            .addComponent(jLabel1, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 418, GroupLayout.PREFERRED_SIZE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jLabel3))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPasswordField1, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(scanQrButton, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)))
                .addGap(40, 40, 40))
        );

        // jPanel4 (the card) stays fixed-size and floats centered within
        // jPanel3 (the photo).
        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, Short.MAX_VALUE)
                .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, Short.MAX_VALUE)
                .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new Color(56, 103, 214));

        jTable1.setModel(new DefaultTableModel(
            new Object[][]{{null, null, null, null}, {null, null, null, null}, {null, null, null, null}, {null, null, null, null}},
            new String[]{"Title 1", "Title 2", "Title 3", "Title 4"}
        ));
        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(224, 227, 233), 1));

        jButton4.setText("LOG OUT");
        jButton4.setBackground(new Color(200, 55, 55));
        jButton4.setForeground(Color.WHITE);
        jButton4.setFocusPainted(false);
        jButton4.addActionListener(this::jButton4ActionPerformed);

        // --- Mini stat cards, matching the reference dashboard style ---
        totalStudentsCard = new StatMiniCard("TOTAL STUDENTS", new Color(56, 103, 214));
        presentCard = new StatMiniCard("PRESENT TODAY", new Color(46, 160, 67));
        absentCard = new StatMiniCard("ABSENT TODAY", new Color(200, 55, 55));
        lateCard = new StatMiniCard("LATE TODAY", new Color(230, 140, 30));

        statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.add(totalStudentsCard);
        statsRow.add(presentCard);
        statsRow.add(absentCard);
        statsRow.add(lateCard);

        // Table + LOG OUT button placed side-by-side (mirrors SignupPanel's
        // jPanel1 layout), with the stat cards row underneath.
        // Table + LOG OUT button placed side-by-side, with the stat cards row underneath.
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        // Allow horizontal stretching
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE) 
                        .addGap(20, 20, 20)
                        .addComponent(jButton4))
                    .addComponent(statsRow, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40) // Fixed top margin
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton4)
                    // The Short.MAX_VALUE here is the magic that lets the table expand vertically
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)) 
                .addGap(20, 20, 20) // Gap between table and stats cards
                .addComponent(statsRow, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)) // Fixed bottom margin anchors the stats cards and forces the table to stretch
        );

        // Top-level: both jPanel1 (table) and jPanel3 (photo) resize with
        // the window, sharing extra space proportionally.
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }

    /** Opens the webcam scanner; on a successful scan, fills the fields and reuses the normal login flow. */
    private void openQrScanner() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        new QrScannerDialog(owner, text -> {
            String[] parts = text.split(":", 2);
            if (parts.length != 2) {
                JOptionPane.showMessageDialog(this, "Unrecognized QR code format.");
                return;
            }
            jTextField1.setText(parts[0]);
            jPasswordField1.setText(parts[1]);
            jButton1.doClick();
        });
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        String username = jTextField1.getText().trim();
        String password = String.valueOf(jPasswordField1.getPassword());

        if (UserManager.login(username, password)) {

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            String loginTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));

            ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();

            Attendance existing = null;
            for (Attendance a : attendance) {
                if (a.getUsername().equals(username) && a.getDate().equals(date)) {
                    existing = a;
                    break;
                }
            }

            if (existing != null && existing.getStatus().equals("Logged In")) {
                JOptionPane.showMessageDialog(this, "This user is already logged in.");
                loadTodayAttendance();
                return;
            }
            if (existing != null) {
                attendance.remove(existing);
            }

            String fullName = "";
            for (User u : UserManager.loadUsers()) {
                if (u.getUsername().equals(username)) {
                    fullName = u.getName();
                    break;
                }
            }

            LocalTime limit = LocalTime.of(8, 0);
            String attendanceStatus = LocalTime.now().isAfter(limit) ? "Late" : "Present";

            attendance.add(new Attendance(username, fullName, date, loginTime, "", "Logged In", attendanceStatus));
            AttendanceManager.saveAttendance(attendance);
            ExcelAttendanceLogger.logAttendance(username, fullName, date, loginTime, "", "Logged In", attendanceStatus);

            if ("Late".equals(attendanceStatus)) {
                promptLateReason(username, fullName, date);
            }
            promptAbsenceExcuseIfNeeded(username, fullName, date);

            loadTodayAttendance();
            updateStatCards();
            jTextField1.setText("");
            jPasswordField1.setText("");
            JOptionPane.showMessageDialog(this, "Login Successful!");

        } else if (UserManager.adminLogin(username, password)) {
            JOptionPane.showMessageDialog(this, "Login Successful!");
            jTextField1.setText("");
            jPasswordField1.setText("");
            mainFrame.showCard(MainFrame.CARD_ADMIN);

        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password");
        }
    }

    /** Asks why the student is late, unless they already answered for today. */
    private void promptLateReason(String username, String fullName, String date) {
        if (ReasonLogManager.hasEntry(username, date, "LATE")) {
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
                "You're marked Late today. Why are you late?",
                "Reason for Being Late",
                JOptionPane.QUESTION_MESSAGE);

        if (reason == null || reason.trim().isEmpty()) {
            reason = "(No reason provided)";
        } else {
            reason = reason.trim();
        }

        String loggedAt = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        ReasonLogManager.addEntry(new LogEntry(username, fullName, date, "LATE", reason, loggedAt));
    }

    private LocalDate previousSchoolDay(LocalDate from) {
        LocalDate d = from.minusDays(1);
        while (d.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || d.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            d = d.minusDays(1);
        }
        return d;
    }

    /**
     * If this student has attendance history but no record for the most
     * recent school day before today (skipping weekends), and hasn't
     * already given an excuse for it, asks for an excuse letter. Never
     * blocks login -- a blank/cancelled answer is still logged as
     * "(No excuse provided)" so the gap is visible to admin.
     */
    private void promptAbsenceExcuseIfNeeded(String username, String fullName, String today) {
        String expectedPriorDate = previousSchoolDay(LocalDate.now()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        ArrayList<Attendance> all = AttendanceManager.loadAttendance();
        boolean hasPriorHistory = false;
        boolean hasExpectedDateRecord = false;
        for (Attendance a : all) {
            if (a.getUsername().equals(username)) {
                if (!a.getDate().equals(today)) {
                    hasPriorHistory = true;
                }
                if (a.getDate().equals(expectedPriorDate)) {
                    hasExpectedDateRecord = true;
                }
            }
        }

        if (!hasPriorHistory || hasExpectedDateRecord || ReasonLogManager.hasEntry(username, expectedPriorDate, "ABSENCE")) {
            return;
        }

        JTextArea excuseArea = new JTextArea(5, 30);
        excuseArea.setLineWrap(true);
        excuseArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(excuseArea);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("You were absent on " + expectedPriorDate + ". Please provide an excuse letter:"), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Excuse Letter",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        String excuse = (result == JOptionPane.OK_OPTION) ? excuseArea.getText().trim() : "";
        if (excuse.isEmpty()) {
            excuse = "(No excuse provided)";
        }

        String loggedAt = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        ReasonLogManager.addEntry(new LogEntry(username, fullName, expectedPriorDate, "ABSENCE", excuse, loggedAt));
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] rows = jTable1.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least one user.");
            return;
        }

        if (rows.length > 1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Log out " + rows.length + " selected users?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        ArrayList<Attendance> attendance = AttendanceManager.loadAttendance();

        int loggedOut = 0;
        for (int row : rows) {
            String username = model.getValueAt(row, 0).toString();
            String date = model.getValueAt(row, 1).toString();
            boolean removed = attendance.removeIf(a -> a.getUsername().equals(username) && a.getDate().equals(date));
            if (removed) {
                loggedOut++;
            }
        }
        AttendanceManager.saveAttendance(attendance);

        loadTodayAttendance();
        updateStatCards();
        JOptionPane.showMessageDialog(this, loggedOut + " user(s) logged out.");
    }
}