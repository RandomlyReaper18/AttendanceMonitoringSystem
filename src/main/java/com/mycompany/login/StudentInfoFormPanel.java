package com.mycompany.login;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
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

    // --- Color Palette for Soft, Modern UI ---
    private static final Color PAGE_BG = new Color(240, 243, 246);
    private static final Color CARD_BG = new Color(250, 251, 253);
    private static final Color HEADER_BG = new Color(43, 62, 80);
    private static final Color CARD_BORDER = new Color(210, 216, 224);
    private static final Color TEXT_PRIMARY = new Color(45, 55, 72);
    private static final Color TEXT_MUTED = new Color(100, 110, 125);
    
    // Accent Colors
    private static final Color PRIMARY_BLUE = new Color(56, 103, 214);
    private static final Color SUCCESS_GREEN = new Color(46, 160, 67);
    private static final Color RESET_GRAY = new Color(108, 117, 125);

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
    private JPanel resultsWrapPanel;

    public StudentInfoFormPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
    }

    /** Called by MainFrame right before this card becomes visible. */
    public void onShow() {
        // Kept state between visits
    }

    private static class GeneratedAccount {
        String firstName;
        String lastName;
        String username;
        String password;

        String fullName() {
            return firstName + " " + lastName;
        }
    }

    private void initComponents() {
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        // --- Header Banner ---
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(HEADER_BG);
        titlePanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Student Registration & Account Generator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Paste student names from Excel to automatically generate system credentials.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(190, 205, 220));

        JPanel titleTextContainer = new JPanel();
        titleTextContainer.setLayout(new BoxLayout(titleTextContainer, BoxLayout.Y_AXIS));
        titleTextContainer.setOpaque(false);
        titleTextContainer.add(title);
        titleTextContainer.add(Box.createVerticalStrut(4));
        titleTextContainer.add(subtitle);

        titlePanel.add(titleTextContainer, BorderLayout.WEST);

        // --- Main Form Card ---
        JPanel formCard = new JPanel();
        formCard.setBackground(CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1),
                new EmptyBorder(20, 20, 20, 20)));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        // --- Section / Grade Row ---
        JPanel sectionGradeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        sectionGradeRow.setOpaque(false);
        sectionField = createStyledTextField(14);
        gradeField = createStyledTextField(8);
        
        sectionGradeRow.add(createFieldLabel("Section:"));
        sectionGradeRow.add(sectionField);
        sectionGradeRow.add(Box.createHorizontalStrut(20));
        sectionGradeRow.add(createFieldLabel("Grade Level:"));
        sectionGradeRow.add(gradeField);

        // --- Paste Area ---
        JLabel pasteLabel = createFieldLabel("Paste Student Names List (One name per line):");
        pasteLabel.setBorder(new EmptyBorder(12, 0, 6, 0));

        pasteArea = new JTextArea(10, 40);
        pasteArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        pasteArea.setLineWrap(false);
        pasteArea.setBackground(Color.WHITE);
        pasteArea.setForeground(TEXT_PRIMARY);
        pasteArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane pasteScroll = new JScrollPane(pasteArea);
        pasteScroll.setBorder(new LineBorder(CARD_BORDER, 1));
        pasteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        pasteScroll.setPreferredSize(new Dimension(600, 180));
        pasteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // --- Counts Row ---
        JPanel countsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        countsRow.setOpaque(false);
        studentCountField = createStyledTextField(5);
        girlsField = createStyledTextField(5);
        boysField = createStyledTextField(5);

        countsRow.add(createFieldLabel("Total Students:"));
        countsRow.add(studentCountField);
        countsRow.add(Box.createHorizontalStrut(15));
        countsRow.add(createFieldLabel("Girls:"));
        countsRow.add(girlsField);
        countsRow.add(Box.createHorizontalStrut(15));
        countsRow.add(createFieldLabel("Boys:"));
        countsRow.add(boysField);

        // --- Generate Button ---
        generateButton = new JButton("GENERATE ACCOUNTS");
        styleButton(generateButton, PRIMARY_BLUE, Color.WHITE);
        generateButton.setPreferredSize(new Dimension(200, 36));
        generateButton.addActionListener(e -> onGenerateClicked());

        JPanel generateRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generateRow.setOpaque(false);
        generateRow.setBorder(new EmptyBorder(14, 0, 0, 0));
        generateRow.add(generateButton);

        sectionGradeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pasteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(sectionGradeRow);
        formCard.add(pasteLabel);
        formCard.add(pasteScroll);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(countsRow);
        formCard.add(generateRow);

        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setOpaque(false);
        formWrap.setBorder(new EmptyBorder(16, 24, 0, 24));
        formWrap.add(formCard, BorderLayout.CENTER);

        // --- Results Table Setup ---
        resultsModel = new DefaultTableModel(new String[]{"Username", "Generated Password", "Full Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        resultsTable = new JTable(resultsModel);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setRowHeight(26);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultsTable.getTableHeader().setBackground(new Color(230, 235, 242));
        resultsTable.getTableHeader().setForeground(TEXT_PRIMARY);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        resultsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsScroll.setBorder(new LineBorder(CARD_BORDER, 1));

        JPanel resultsCard = new JPanel(new BorderLayout(0, 10));
        resultsCard.setBackground(CARD_BG);
        resultsCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        copyButton = new JButton("Copy List to Clipboard");
        styleButton(copyButton, Color.WHITE, TEXT_PRIMARY);
        copyButton.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER, 1), new EmptyBorder(6, 12, 6, 12)));

        saveButton = new JButton("Save & Register All");
        styleButton(saveButton, SUCCESS_GREEN, Color.WHITE);

        openQrFolderButton = new JButton("Open QR Folder");
        styleButton(openQrFolderButton, Color.WHITE, TEXT_PRIMARY);
        openQrFolderButton.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER, 1), new EmptyBorder(6, 12, 6, 12)));
        openQrFolderButton.setEnabled(false);

        copyButton.addActionListener(e -> copyResultsToClipboard());
        saveButton.addActionListener(e -> onSaveClicked());
        openQrFolderButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(QrCodeGenerator.resolveQrFolder().toFile());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Couldn't open the folder: " + ex.getMessage());
            }
        });

        JPanel resultsButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        resultsButtonRow.setOpaque(false);
        resultsButtonRow.add(saveButton);
        resultsButtonRow.add(copyButton);
        resultsButtonRow.add(openQrFolderButton);

        JPanel resultsTop = new JPanel(new BorderLayout(0, 8));
        resultsTop.setOpaque(false);
        resultsTop.add(statusLabel, BorderLayout.NORTH);
        resultsTop.add(resultsButtonRow, BorderLayout.SOUTH);

        resultsCard.add(resultsTop, BorderLayout.NORTH);
        resultsCard.add(resultsScroll, BorderLayout.CENTER);

        JPanel resultsWrap = new JPanel(new BorderLayout());
        resultsWrap.setOpaque(false);
        resultsWrap.setBorder(new EmptyBorder(16, 24, 0, 24));
        resultsWrap.add(resultsCard, BorderLayout.CENTER);
        resultsWrap.setVisible(false);

        // --- Bottom Command Row ---
        resetButton = new JButton("Start New Section");
        styleButton(resetButton, RESET_GRAY, Color.WHITE);
        resetButton.addActionListener(e -> resetForm());

        backButton = new JButton("Back to Dashboard");
        styleButton(backButton, PRIMARY_BLUE, Color.WHITE);
        backButton.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_ADMIN));

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(12, 24, 16, 24));
        bottomRow.add(backButton);
        bottomRow.add(resetButton);

        // --- Outer Container Assembly ---
        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        formWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerStack.add(formWrap);
        centerStack.add(resultsWrap);

        JScrollPane outerScroll = new JScrollPane(centerStack);
        outerScroll.setBorder(null);
        outerScroll.setOpaque(false);
        outerScroll.getViewport().setOpaque(false);
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(titlePanel, BorderLayout.NORTH);
        add(outerScroll, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);

        this.resultsWrapPanel = resultsWrap;
    }

    // --- Modern Component Styling Helpers ---
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(Color.WHITE);
        field.setBorder(new CompoundBorder(
                new LineBorder(CARD_BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)));
        return field;
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
    }

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
        statusLabel.setForeground(TEXT_PRIMARY);
        resultsWrapPanel.setVisible(true);
        saveButton.setEnabled(true);
    }

    private static class ParsedName {
        String firstName;
        String lastName;
    }

    private List<ParsedName> parseNames(String pastedText, List<String> badLinesOut) {
        List<ParsedName> result = new ArrayList<>();
        if (pastedText == null) {
            return result;
        }

        String[] lines = pastedText.split("\r?\n");
        for (String rawLine : lines) {
            String original = rawLine.trim();
            if (original.isEmpty()) {
                continue;
            }

            String cleaned = original
                    .replaceFirst("^\\d+[.)]\\s*", "")
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

    private List<GeneratedAccount> generateAccounts(List<ParsedName> names) {
        Set<String> usedUsernames = new HashSet<>();
        for (User u : UserManager.loadUsers()) {
            usedUsernames.add(u.getUsername().toLowerCase());
        }

        List<GeneratedAccount> accounts = new ArrayList<>();
        for (ParsedName pn : names) {
            String base = (pn.firstName + "." + pn.lastName)
                    .toLowerCase()
                    .replaceAll("[^a-z.]", "");

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

    private void onSaveClicked() {
        if (pendingAccounts.isEmpty()) {
            return;
        }

        int created = 0;
        int qrGenerated = 0;
        List<String> failed = new ArrayList<>();
        List<String> qrFailed = new ArrayList<>();
        String section = sectionField.getText().trim();
        String grade = gradeField.getText().trim();

        for (GeneratedAccount acc : pendingAccounts) {
            boolean ok = UserManager.register(acc.username, acc.password, acc.fullName());
            if (ok) {
                created++;
                StudentSectionManager.assignSection(acc.username, section, grade);
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
               .append(" account(s) failed (username already existed):\n");
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
        statusLabel.setForeground(SUCCESS_GREEN);
        saveButton.setEnabled(false);
        openQrFolderButton.setEnabled(true);
    }

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