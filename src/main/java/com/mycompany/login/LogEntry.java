package com.mycompany.login;

/**
 * One answer a student gave -- either why they were late, or an excuse
 * letter for a day they were absent. Stored by ReasonLogManager.
 */
public class LogEntry {
    private String username;
    private String fullName;
    private String date;     // the date this entry is ABOUT (MM/dd/yyyy)
    private String type;     // "LATE" or "ABSENCE"
    private String answer;
    private String loggedAt; // time the answer was actually submitted

    public LogEntry() {
    }

    public LogEntry(String username, String fullName, String date, String type, String answer, String loggedAt) {
        this.username = username;
        this.fullName = fullName;
        this.date = date;
        this.type = type;
        this.answer = answer;
        this.loggedAt = loggedAt;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public String getAnswer() { return answer; }
    public String getLoggedAt() { return loggedAt; }
}   