package com.mycompany.login;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Lets a teacher/admin paste a class list (one full name per line, copied
 * from Excel), enter Section/Grade and Girls/Boys counts, and generate a
 * username + password for every student in one go. Generated accounts
 * are registered locally so students can log in to this app.
 */
public class StudentInfoFormPanel extends JPanel {

    private static final Color PAGE_BG = new Color(244, 246, 250);
    private static final Color CARD_BORDER = new Color(224, 227, 233);

    private final MainFrame mainFrame;
    private final Random random = new Random();

    private JTextField sectionField;
    private JTextField gradeField;
    private JTextArea pasteArea;
    private JTextField studentCountField;
    private JTextField girlsField;
    private JTextField boysField;
    private JButton generateButton;
    private JButton copyButton;
    private JButton openQrFolderButton;
    private JButton saveButton;
    private JButton resetButton;
    private JButton backButton;
    private JLabel statusLabel;

    private JTable resultsTable;
    private DefaultTableModel resultsModel;

    private List<GeneratedAccount> pendingAccounts = new ArrayList<>();

    public StudentInfoFormPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    /** Called by MainFrame right before this card becomes visible. */
    public void onShow() {
        // Intentionally left as-is between visits so a teacher/admin can
        // leave and come back without losing an in-progress paste.
    }

    // ---------------------------------------------------------------
    // Data holder for one generated student account
    // ---------------------------------------------------------------
    private static class GeneratedAccount {
        String firstName;
        String lastName;
        String username;
        String password;

        String fullName() {
            return firstName + " " + lastName;
        }
    }

    // ---------------------------------------------------------------
    // UI construction
    // ---------------------------------------------------------------
    private void initComponents() {
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Student Info Form");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 24, 4, 24));

        JPanel formCard = new JPanel();
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        // --- Section / Grade row ---
        JPanel sectionGradeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        sectionGradeRow.setOpaque(false);
        sectionField = new JTextField(14);
        gradeField = new JTextField(8);
        sectionGradeRow.add(new JLabel("Section:"));
        sectionGradeRow.add(sectionField);
        sectionGradeRow.add(Box.createHorizontalStrut(16));
        sectionGradeRow.add(new JLabel("Grade:"));
        sectionGradeRow.add(gradeField);

        // --- Paste box ---
        JLabel pasteLabel = new JLabel("Paste all the names of your students here (from Excel):");
        pasteLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 0));
        pasteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        pasteArea = new JTextArea(10, 40);
        pasteArea.setLineWrap(false);
        pasteArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane pasteScroll = new JScrollPane(pasteArea);
        pasteScroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        pasteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        pasteScroll.setPreferredSize(new Dimension(600, 220));
        pasteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        // --- Counts row ---
        JPanel countsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        countsRow.setOpaque(false);
        studentCountField = new JTextField(5);
        girlsField = new JTextField(5);
        boysField = new JTextField(5);
        countsRow.add(new JLabel("No. of Students:"));
        countsRow.add(studentCountField);
        countsRow.add(Box.createHorizontalStrut(16));
        countsRow.add(new JLabel("Girls:"));
        countsRow.add(girlsField);
        countsRow.add(Box.createHorizontalStrut(16));
        countsRow.add(new JLabel("Boys:"));
        countsRow.add(boysField);

        // --- Generate button ---
        generateButton = new JButton("GENERATE ACCOUNTS");
        generateButton.setBackground(new Color(56, 103, 214));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.setFont(generateButton.getFont().deriveFont(Font.BOLD));
        generateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateButton.addActionListener(e -> onGenerateClicked());

        JPanel generateRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generateRow.setOpaque(false);
        generateRow.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        generateRow.add(generateButton);

        sectionGradeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        countsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(sectionGradeRow);
        formCard.add(pasteLabel);
        formCard.add(pasteScroll);
        formCard.add(countsRow);
        formCard.add(generateRow);

        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setOpaque(false);
        formWrap.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        formWrap.add(formCard, BorderLayout.CENTER);

        // --- Results table (appears after generating) ---
        resultsModel = new DefaultTableModel(new String[]{"Username", "Password", "Full Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        resultsTable = new JTable(resultsModel);
        resultsTable.setFillsViewportHeight(true);
        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsScroll.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));

        JPanel resultsCard = new JPanel(new BorderLayout(0, 8));
        resultsCard.setBackground(Color.WHITE);
        resultsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        copyButton = new JButton("Copy List to Clipboard");
        saveButton = new JButton("Save && Register All");
        saveButton.setBackground(new Color(46, 160, 67));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        copyButton.addActionListener(e -> copyResultsToClipboard());
        saveButton.addActionListener(e -> onSaveClicked());

        openQrFolderButton = new JButton("Open QR Codes Folder");
        openQrFolderButton.setEnabled(false);
        openQrFolderButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(QrCodeGenerator.resolveQrFolder().toFile());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Couldn't open the folder: " + ex.getMessage());
            }
        });

        JPanel resultsButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resultsButtonRow.setOpaque(false);
        resultsButtonRow.add(copyButton);
        resultsButtonRow.add(saveButton);
        resultsButtonRow.add(openQrFolderButton);

        JPanel resultsTop = new JPanel(new BorderLayout());
        resultsTop.setOpaque(false);
        resultsTop.add(statusLabel, BorderLayout.NORTH);
        resultsTop.add(resultsButtonRow, BorderLayout.SOUTH);

        resultsCard.add(resultsTop, BorderLayout.NORTH);
        resultsCard.add(resultsScroll, BorderLayout.CENTER);

        JPanel resultsWrap = new JPanel(new BorderLayout());
        resultsWrap.setOpaque(false);
        resultsWrap.setBorder(BorderFactory.createEmptyBorder(14, 24, 0, 24));
        resultsWrap.add(resultsCard, BorderLayout.CENTER);
        resultsWrap.setVisible(false); // shown once accounts are generated

        // --- Bottom buttons ---
        resetButton = new JButton("Start New Section");
        resetButton.addActionListener(e -> resetForm());

        backButton = new JButton("Back to Dashboard");
        backButton.setBackground(new Color(56, 103, 214));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_ADMIN));

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomRow.setOpaque(false);
        bottomRow.setBorder(BorderFactory.createEmptyBorder(14, 24, 20, 24));
        bottomRow.add(backButton);
        bottomRow.add(resetButton);

        // --- Assemble ---
        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        formWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerStack.add(formWrap);
        centerStack.add(resultsWrap);

        JScrollPane outerScroll = new JScrollPane(centerStack);
        outerScroll.setBorder(null);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(title, BorderLayout.NORTH);
        add(outerScroll, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);

        // Stash a reference so onGenerateClicked/onSaveClicked can toggle it.
        this.resultsWrapPanel = resultsWrap;
    }

    private JPanel resultsWrapPanel;

    // ---------------------------------------------------------------
    // Generate button flow
    // ---------------------------------------------------------------
    private void onGenerateClicked() {
        String section = sectionField.getText().trim();
        String grade = gradeField.getText().trim();

        if (section.isEmpty() || grade.isEmpty()) {
            showError("Please fill in both Section and Grade.");
            return;
        }

        Integer expectedCount = parsePositiveInt(studentCountField.getText());
        Integer girls = parsePositiveInt(girlsField.getText());
        Integer boys = parsePositiveInt(boysField.getText());

        if (expectedCount == null) {
            showError("\"No. of Students\" must be a whole number.");
            return;
        }
        if (girls == null || boys == null) {
            showError("Girls and Boys must both be whole numbers.");
            return;
        }
        if (girls + boys != expectedCount) {
            showError("Girls (" + girls + ") + Boys (" + boys + ") = " + (girls + boys)
                    + ", which doesn't match No. of Students (" + expectedCount + ").\n"
                    + "Please fix the counts before generating accounts.");
            return;
        }

        List<String> badLines = new ArrayList<>();
        List<ParsedName> parsedNames = parseNames(pasteArea.getText(), badLines);

        if (parsedNames.isEmpty()) {
            showError("No valid student names were found in the pasted list.");
            return;
        }

        if (parsedNames.size() != expectedCount) {
            StringBuilder msg = new StringBuilder();
            msg.append("The pasted list has ").append(parsedNames.size())
               .append(" valid name(s), but No. of Students is set to ").append(expectedCount).append(".\n");
            if (!badLines.isEmpty()) {
                msg.append("\nThese line(s) could not be read and were skipped:\n");
                for (String bad : badLines) {
                    msg.append("  \u2022 ").append(bad).append("\n");
                }
            }
            msg.append("\nPlease fix the list or the count before generating accounts.");
            showError(msg.toString());
            return;
        }

        if (!badLines.isEmpty()) {
            // Shouldn't normally reach here since counts would already
            // mismatch, but just in case: surface it as a warning.
            StringBuilder msg = new StringBuilder("Some lines were skipped:\n");
            for (String bad : badLines) {
                msg.append("  \u2022 ").append(bad).append("\n");
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Skipped Lines", JOptionPane.WARNING_MESSAGE);
        }

        pendingAccounts = generateAccounts(parsedNames);
        populateResultsTable(pendingAccounts);

        statusLabel.setText(pendingAccounts.size() + " account(s) generated for "
                + section + " \u2014 Grade " + grade + ". Review below, then Save.");
        statusLabel.setForeground(new Color(30, 30, 30));
        resultsWrapPanel.setVisible(true);
        saveButton.setEnabled(true);
    }

    // ---------------------------------------------------------------
    // Name parsing
    // ---------------------------------------------------------------
    private static class ParsedName {
        String firstName;
        String lastName;
    }

    /**
     * Parses one name per line. Strips common Excel-paste artifacts
     * (leading numbering like "1.", stray tabs). Lines that can't be
     * split into at least a first and last name are skipped and added
     * to badLinesOut with the original text.
     */
    private List<ParsedName> parseNames(String pastedText, List<String> badLinesOut) {
        List<ParsedName> result = new ArrayList<>();
        if (pastedText == null) {
            return result;
        }

        String[] lines = pastedText.split("\r?\n");
        for (String rawLine : lines) {
            String original = rawLine.trim();
            if (original.isEmpty()) {
                continue; // blank lines are ignored silently, not an error
            }

            String cleaned = original
                    .replaceFirst("^\\d+[.)]\\s*", "") // strip leading "1. " / "1) "
                    .replaceAll("\\t+", " ")
                    .trim();

            String[] parts = cleaned.split("\\s+");
            if (parts.length < 2) {
                badLinesOut.add(original);
                continue;
            }

            ParsedName pn = new ParsedName();
            pn.firstName = parts[0];
            StringBuilder last = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) last.append(" ");
                last.append(parts[i]);
            }
            pn.lastName = last.toString();
            result.add(pn);
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Credential generation
    // ---------------------------------------------------------------
    private List<GeneratedAccount> generateAccounts(List<ParsedName> names) {
        Set<String> usedUsernames = new HashSet<>();
        for (User u : UserManager.loadUsers()) {
            usedUsernames.add(u.getUsername().toLowerCase());
        }

        List<GeneratedAccount> accounts = new ArrayList<>();
        for (ParsedName pn : names) {
            String base = (pn.firstName + "." + pn.lastName)
                    .toLowerCase()
                    .replaceAll("[^a-z.]", ""); // strip spaces/punctuation/apostrophes etc.

            String username = base;
            int suffix = 2;
            while (usedUsernames.contains(username)) {
                username = base + suffix;
                suffix++;
            }
            usedUsernames.add(username);

            GeneratedAccount acc = new GeneratedAccount();
            acc.firstName = pn.firstName;
            acc.lastName = pn.lastName;
            acc.username = username;
            acc.password = String.format("%04d", random.nextInt(10000));
            accounts.add(acc);
        }
        return accounts;
    }

    private void populateResultsTable(List<GeneratedAccount> accounts) {
        resultsModel.setRowCount(0);
        for (GeneratedAccount acc : accounts) {
            resultsModel.addRow(new Object[]{acc.username, acc.password, acc.fullName()});
        }
    }

    // ---------------------------------------------------------------
    // Save / register flow
    // ---------------------------------------------------------------
    private void onSaveClicked() {
        if (pendingAccounts.isEmpty()) {
            return;
        }

        int created = 0;
        int qrGenerated = 0;
        List<String> failed = new ArrayList<>();
        List<String> qrFailed = new ArrayList<>();

        for (GeneratedAccount acc : pendingAccounts) {
            boolean ok = UserManager.register(acc.username, acc.password, acc.fullName());
            if (ok) {
                created++;
                try {
                    QrCodeGenerator.saveForStudent(acc.username, acc.password);
                    qrGenerated++;
                } catch (Exception e) {
                    e.printStackTrace();
                    qrFailed.add(acc.username);
                }
            } else {
                failed.add(acc.username);
            }
        }

        StringBuilder msg = new StringBuilder();
        msg.append(created).append(" account(s) created successfully.\n");
        msg.append(qrGenerated).append(" QR code(s) generated in: ").append(QrCodeGenerator.resolveQrFolder()).append("\n");
        if (!failed.isEmpty()) {
            msg.append("\n").append(failed.size())
               .append(" account(s) failed (username already existed \u2014 shouldn't normally happen):\n");
            for (String f : failed) {
                msg.append("  \u2022 ").append(f).append("\n");
            }
        }
        if (!qrFailed.isEmpty()) {
            msg.append("\n").append(qrFailed.size()).append(" QR code(s) failed to generate:\n");
            for (String f : qrFailed) {
                msg.append("  \u2022 ").append(f).append("\n");
            }
        }
        JOptionPane.showMessageDialog(this, msg.toString(), "Accounts Saved", JOptionPane.INFORMATION_MESSAGE);

        statusLabel.setText("\u2713 Saved. " + created + " account(s) registered for "
                + sectionField.getText().trim() + " \u2014 Grade " + gradeField.getText().trim() + ".");
        statusLabel.setForeground(new Color(46, 160, 67));
        saveButton.setEnabled(false);
        openQrFolderButton.setEnabled(true);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void copyResultsToClipboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("Username\tPassword\tFull Name\n");
        for (GeneratedAccount acc : pendingAccounts) {
            sb.append(acc.username).append("\t").append(acc.password).append("\t").append(acc.fullName()).append("\n");
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        JOptionPane.showMessageDialog(this, "Copied to clipboard \u2014 you can paste this into Excel.");
    }

    private void resetForm() {
        sectionField.setText("");
        gradeField.setText("");
        pasteArea.setText("");
        studentCountField.setText("");
        girlsField.setText("");
        boysField.setText("");
        pendingAccounts = new ArrayList<>();
        resultsModel.setRowCount(0);
        resultsWrapPanel.setVisible(false);
        statusLabel.setText(" ");
        saveButton.setEnabled(true);
    }

    private Integer parsePositiveInt(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return value >= 0 ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Please Check the Form", JOptionPane.ERROR_MESSAGE);
    }
}