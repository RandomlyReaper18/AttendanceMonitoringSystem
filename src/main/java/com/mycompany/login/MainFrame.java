package com.mycompany.login;

import javax.swing.*;
import java.awt.*;

/**
 * The single window for the whole app. Screens (Login, Signup, Admin) are
 * JPanels swapped in and out via CardLayout. Launches maximized so the
 * app fills the screen on startup.
 */
public class MainFrame extends JFrame {

    public static final String CARD_LOGIN = "login";
    public static final String CARD_SIGNUP = "signup";
    public static final String CARD_ADMIN = "admin";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardHost = new JPanel(cardLayout);

    private final LoginPanel loginPanel;
    private final SignupPanel signupPanel;
    private final AdministratorPanel adminPanel;

    public MainFrame() {
        setTitle("Attendance Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon(
                getClass().getResource("/com/mycompany/login/images/Icon.png")
        ).getImage());

        loginPanel = new LoginPanel(this);
        signupPanel = new SignupPanel(this);
        adminPanel = new AdministratorPanel(this);

        cardHost.add(loginPanel, CARD_LOGIN);
        cardHost.add(signupPanel, CARD_SIGNUP);
        cardHost.add(adminPanel, CARD_ADMIN);

        setContentPane(cardHost);

        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 650);
        setLocationRelativeTo(null);

        // Launch fullscreen (maximized, keeps title bar / minimize / close).
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    /** Switch to the given card, refreshing that screen's data first. */
    public void showCard(String name) {
        switch (name) {
            case CARD_LOGIN -> loginPanel.onShow();
            case CARD_SIGNUP -> signupPanel.onShow();
            case CARD_ADMIN -> adminPanel.onShow();
        }
        cardLayout.show(cardHost, name);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}