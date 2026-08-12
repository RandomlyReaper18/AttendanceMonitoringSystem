package com.mycompany.login.JFRAME;

import com.mycompany.login.Attendance;
import com.mycompany.login.AttendanceManager;
import com.mycompany.login.BackgroundPanel;
import com.mycompany.login.MainFrame;
import com.mycompany.login.StatMiniCard;
import com.mycompany.login.TranslucentCardPanel;
import com.mycompany.login.User;
import com.mycompany.login.UserManager;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * The sign-up + live attendance table screen. Now includes the same
 * mini stat-card row as LoginPanel, underneath the table.
 */
public class SignupPanel extends JPanel {

    private final MainFrame mainFrame;
    private int currentRow = -1;

    private BackgroundPanel jPanel3;
    private JPanel jPanel2; // TranslucentCardPanel, the signup card
    private JTextField jTextField2; // username
    private JLabel jLabel2;
    private JLabel jLabel5;
    private JPasswordField jPasswordField2; // password
    private JLabel jLabel3;
    private JPasswordField jPasswordField1; // repeat password
    private JLabel jLabel6;
    private JTextField jTextField1; // full name
    private JButton jButton2; // SIGN UP submit
    private JLabel jLabel4;
    private JButton jButton1; // LOGIN link
    private JLabel jLabel1;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JTable jTable1;
    private JButton jButton4; // LOG OUT

    private JPanel statsRow;
    private StatMiniCard totalStudentsCard;
    private StatMiniCard presentCard;
    private StatMiniCard absentCard;
    private StatMiniCard lateCard;

    public SignupPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
        jPanel3.setBackgroundImage("/com/mycompany/login/images/signal.jpg");

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Username");
        model.addColumn("Date");
        model.addColumn("Login Time");
        model.addColumn("Logout Time");
        model.addColumn("Status");
        jTable1.setModel(model);

        jPasswordField1.addActionListener(e -> jButton2.doClick());

        loadTodayAttendance();
        updateStatCards();
    }

    /** Called by MainFrame right before this card becomes visible. */
    public void onShow() {
        jTextField2.setText("");
        jPasswordField1.setText("");
        jPasswordField2.setText("");
        jTextField1.setText("");
        loadTodayAttendance();
        updateStatCards();
    }

    private boolean isStrongPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasNumber = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasNumber = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasNumber && hasSpecial;
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
        jPanel2 = new TranslucentCardPanel();
        jTextField2 = new JTextField();
        jLabel2 = new JLabel();
        jLabel5 = new JLabel();
        jPasswordField2 = new JPasswordField();
        jLabel3 = new JLabel();
        jPasswordField1 = new JPasswordField();
        jLabel6 = new JLabel();
        jTextField1 = new JTextField();
        jButton2 = new JButton();
        jLabel4 = new JLabel();
        jButton1 = new JButton();
        jLabel1 = new JLabel();
        jPanel1 = new JPanel();
        jScrollPane1 = new JScrollPane();
        jTable1 = new JTable();
        jButton4 = new JButton();

        jLabel2.setFont(new Font("Tahoma", Font.BOLD, 10));
        jLabel2.setText("Username: ");

        jLabel5.setFont(new Font("Tahoma", Font.BOLD, 10));
        jLabel5.setText("Password: ");

        jLabel3.setFont(new Font("Tahoma", Font.BOLD, 10));
        jLabel3.setText("Repeat Password: ");

        jLabel6.setFont(new Font("Tahoma", Font.BOLD, 10));
        jLabel6.setText("Full Name:");

        jButton2.setText("SIGN UP");
        jButton2.setBackground(new Color(56, 103, 214));
        jButton2.setForeground(Color.WHITE);
        jButton2.setFocusPainted(false);
        jButton2.setFont(jButton2.getFont().deriveFont(Font.BOLD));
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jLabel4.setText("I already have an account.");
        jLabel4.setForeground(new Color(90, 90, 90));

        jButton1.setText("LOGIN");
        jButton1.setBackground(Color.WHITE);
        jButton1.setForeground(new Color(56, 103, 214));
        jButton1.setFocusPainted(false);
        jButton1.setBorder(BorderFactory.createLineBorder(new Color(56, 103, 214), 1));
        jButton1.addActionListener(this::jButton1ActionPerformed);

        Border fieldBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 227, 233), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6));
        jTextField2.setBorder(fieldBorder);
        jPasswordField2.setBorder(fieldBorder);
        jPasswordField1.setBorder(fieldBorder);
        jTextField1.setBorder(fieldBorder);

        jLabel1.setFont(new Font("Calibri", Font.BOLD, 24));
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText("SIGN UP");

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 66, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                        .addGap(146, 146, 146))
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addGap(265, 265, 265))
                        .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jPasswordField1, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPasswordField2, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE))
                        .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 259, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 259, GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 215, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(jLabel5)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField2, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField1, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        // jPanel2 (the card) stays fixed-size and floats centered within
        // jPanel3 (the photo).
        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, Short.MAX_VALUE)
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, Short.MAX_VALUE)
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new Color(56, 103, 214));

        jTable1.setModel(new DefaultTableModel(
            new Object[][]{{null, null, null, null}, {null, null, null, null}, {null, null, null, null}, {null, null, null, null}},
            new String[]{"Title 1", "Title 2", "Title 3", "Title 4"}
        ));
        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.setPreferredSize(new Dimension(400, 280));
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(224, 227, 233), 1));

        jButton4.setText("LOG OUT");
        jButton4.setBackground(new Color(200, 55, 55));
        jButton4.setForeground(Color.WHITE);
        jButton4.setFocusPainted(false);
        jButton4.addActionListener(this::jButton4ActionPerformed);

        // --- Mini stat cards, matching LoginPanel / the reference dashboard ---
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

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(29, 29, 29)
                        .addComponent(jButton4))
                    .addComponent(statsRow, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton4)
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(statsRow, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        // Top-level: both sides resize with the window.
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        mainFrame.showCard(MainFrame.CARD_LOGIN);
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        String username = jTextField2.getText();
        String password = String.valueOf(jPasswordField1.getPassword());
        String confirm = String.valueOf(jPasswordField2.getPassword());
        String name = jTextField1.getText();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields");
            return;
        }

        if (!isStrongPassword(password)) {
            JOptionPane.showMessageDialog(this,
                    "Password must contain:\n\n"
                    + "- Minimum 8 characters\n"
                    + "- At least 1 uppercase letter\n"
                    + "- At least 1 lowercase letter\n"
                    + "- At least 1 number\n"
                    + "- At least 1 special character");
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }

        boolean ok = UserManager.register(username, password, name);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Account Created!");
            mainFrame.showCard(MainFrame.CARD_LOGIN);
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists");
        }
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentRow == -1) {
            return;
        }
        String logoutTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        jTable1.setValueAt(logoutTime, currentRow, 3);
        jTable1.setValueAt("Logged Out", currentRow, 4);
        currentRow = -1;
    }
}