package com.mycompany.login;

/** Which section/grade a student belongs to. */
public class StudentSection {
    private String username;
    private String section;
    private String grade;

    public StudentSection() {
    }

    public StudentSection(String username, String section, String grade) {
        this.username = username;
        this.section = section;
        this.grade = grade;
    }

    public String getUsername() { return username; }
    public String getSection() { return section; }
    public String getGrade() { return grade; }
}