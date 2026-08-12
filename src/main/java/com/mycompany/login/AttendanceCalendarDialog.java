package com.mycompany.login;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A month-at-a-glance attendance view for one student. Present days are
 * green, Late days orange, unexplained weekday absences red, and
 * weekends/future/no-data days neutral gray.
 */
public class AttendanceCalendarDialog extends JDialog {

    private static final Color PRESENT_COLOR = new Color(46, 160, 67);
    private static final Color LATE_COLOR = new Color(230, 140, 30);
    private static final Color ABSENT_COLOR = new Color(200, 55, 55);
    private static final Color NEUTRAL_COLOR = new Color(238, 240, 244);
    private static final Color TODAY_BORDER = new Color(56, 103, 214);

    private final String username;
    private YearMonth currentMonth;

    private JLabel monthLabel;
    private JPanel gridPanel;

    public AttendanceCalendarDialog(Window owner, String username, String fullName) {
        super(owner, "Attendance Calendar \u2014 " + fullName, ModalityType.APPLICATION_MODAL);
        this.username = username;
        this.currentMonth = YearMonth.now();

        buildUI();
        refreshCalendar();

        setSize(580, 540);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 10));
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setContentPane(content);

        JButton prevButton = new JButton("\u2039 Prev");
        JButton nextButton = new JButton("Next \u203a");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(prevButton, BorderLayout.WEST);
        headerRow.add(monthLabel, BorderLayout.CENTER);
        headerRow.add(nextButton, BorderLayout.EAST);

        gridPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        gridPanel.setOpaque(false);

        add(headerRow, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        legend.setOpaque(false);
        legend.add(legendItem("Present", PRESENT_COLOR));
        legend.add(legendItem("Late", LATE_COLOR));
        legend.add(legendItem("Absent", ABSENT_COLOR));
        legend.add(legendItem("Weekend / No Data", NEUTRAL_COLOR));
        return legend;
    }

    private JPanel legendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(false);
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(14, 14));
        swatch.setBackground(color);
        swatch.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        item.add(swatch);
        item.add(new JLabel(label));
        return item;
    }

    private void refreshCalendar() {
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + currentMonth.getYear());

        Map<String, String> statusByDate = loadStatusForMonth();

        gridPanel.removeAll();

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : dayNames) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            gridPanel.add(lbl);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        // DayOfWeek: Monday=1 ... Sunday=7. We want a Sunday-first grid,
        // so Sunday needs 0 leading blanks, Monday needs 1, etc.
        int leadingBlanks = firstOfMonth.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < leadingBlanks; i++) {
            gridPanel.add(new JLabel(""));
        }

        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            String dateKey = date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            boolean isFuture = date.isAfter(today);
            String status = statusByDate.get(dateKey);

            Color bg;
            if ("Present".equals(status)) {
                bg = PRESENT_COLOR;
            } else if ("Late".equals(status)) {
                bg = LATE_COLOR;
            } else if (!isWeekend && !isFuture) {
                bg = ABSENT_COLOR;
            } else {
                bg = NEUTRAL_COLOR;
            }

            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(bg);
            cell.setBorder(date.equals(today)
                    ? BorderFactory.createLineBorder(TODAY_BORDER, 2)
                    : BorderFactory.createLineBorder(new Color(220, 220, 220)));
            cell.setPreferredSize(new Dimension(60, 55));

            boolean darkBg = (bg == PRESENT_COLOR || bg == LATE_COLOR || bg == ABSENT_COLOR);
            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setForeground(darkBg ? Color.WHITE : new Color(60, 60, 60));
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD));
            cell.add(dayLabel, BorderLayout.CENTER);

            gridPanel.add(cell);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private Map<String, String> loadStatusForMonth() {
        Map<String, String> map = new HashMap<>();
        ArrayList<Attendance> all = AttendanceManager.loadAttendance();
        for (Attendance a : all) {
            if (!a.getUsername().equals(username)) {
                continue;
            }
            try {
                LocalDate d = LocalDate.parse(a.getDate(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                if (YearMonth.from(d).equals(currentMonth)) {
                    map.put(a.getDate(), a.getAttendanceStatus());
                }
            } catch (Exception ignored) {
                // Unparseable date -- skip it rather than crash the calendar.
            }
        }
        return map;
    }
}