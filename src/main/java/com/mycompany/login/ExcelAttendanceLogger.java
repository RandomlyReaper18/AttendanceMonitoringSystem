package com.mycompany.login;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Mirrors each day's attendance into an Excel workbook (attendance_log.xlsx),
 * one sheet per day. This runs IN ADDITION to AttendanceManager --
 * attendance.json stays the source of truth for the app's own UI (stat
 * cards, admin table, calendar view); this class exists purely so a
 * teacher/admin can open a normal, familiar Excel file.
 *
 * Requires Apache POI on the classpath (poi + poi-ooxml, see the setup
 * note at the bottom of this file).
 */
public class ExcelAttendanceLogger {

    private static final String FILE_NAME = "attendance_log.xlsx";
    private static final String[] HEADERS = {
        "Username", "Full Name", "Date", "Login Time", "Logout Time", "Status", "Attendance Status"
    };

    private static Path resolveDataFile() {
        try {
            String jarDir = new File(ExcelAttendanceLogger.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .getAbsolutePath();
            return Paths.get(jarDir, FILE_NAME);
        } catch (Exception e) {
            return Paths.get(FILE_NAME);
        }
    }

    /**
     * Records (or updates) one attendance row on the sheet for that date.
     * Safe to call every time a student logs in -- it finds the existing
     * row for that username on that date and updates it instead of
     * duplicating, matching AttendanceManager's own dedupe behavior.
     */
    public static synchronized void logAttendance(
            String username, String fullName, String date,
            String loginTime, String logoutTime, String status, String attendanceStatus) {

        Path file = resolveDataFile();
        Path tempFile = Paths.get(file.toString() + ".tmp");

        try (Workbook workbook = openOrCreateWorkbook(file)) {

            String sheetName = sanitizeSheetName(date);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
                writeHeaderRow(sheet);
            }

            Row targetRow = findRowForUsername(sheet, username);
            if (targetRow == null) {
                targetRow = sheet.createRow(sheet.getLastRowNum() + 1);
            }

            setCell(targetRow, 0, username);
            setCell(targetRow, 1, fullName);
            setCell(targetRow, 2, date);
            setCell(targetRow, 3, loginTime == null ? "" : loginTime);
            setCell(targetRow, 4, logoutTime == null ? "" : logoutTime);
            setCell(targetRow, 5, status == null ? "" : status);
            setCell(targetRow, 6, attendanceStatus == null ? "" : attendanceStatus);

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (OutputStream os = Files.newOutputStream(tempFile)) {
                workbook.write(os);
            }
            Files.move(tempFile, file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to write attendance to Excel: " + file.toAbsolutePath());
        }
    }

    private static Workbook openOrCreateWorkbook(Path file) throws IOException {
        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                return new XSSFWorkbook(is);
            }
        }
        return new XSSFWorkbook();
    }

    private static void writeHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            header.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    private static Row findRowForUsername(Sheet sheet, String username) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue; // header row
            }
            Cell cell = row.getCell(0);
            if (cell != null && username.equals(cell.getStringCellValue())) {
                return row;
            }
        }
        return null;
    }

    private static void setCell(Row row, int index, String value) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            cell = row.createCell(index);
        }
        cell.setCellValue(value);
    }

    /** Excel sheet names can't contain \ / ? * [ ] : and must be 31 chars or fewer. */
    private static String sanitizeSheetName(String date) {
        String cleaned = date.replaceAll("[\\\\/?*\\[\\]:]", "-");
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }
}

/*
 * SETUP: this needs Apache POI on the classpath.
 *
 * If your project uses Maven, add to pom.xml:
 *
 *   <dependency>
 *       <groupId>org.apache.poi</groupId>
 *       <artifactId>poi-ooxml</artifactId>
 *       <version>5.2.5</version>
 *   </dependency>
 *
 * If you're managing jars manually in NetBeans (Project Properties ->
 * Libraries -> Add JAR/Folder), download these from Maven Central and
 * add them all (poi-ooxml pulls in several transitive dependencies):
 *   - poi-5.2.5.jar
 *   - poi-ooxml-5.2.5.jar
 *   - poi-ooxml-lite-5.2.5.jar
 *   - xmlbeans-5.2.0.jar
 *   - commons-collections4-4.4.jar
 *   - commons-compress-1.26.0.jar
 *   - commons-io-2.15.1.jar
 *   - log4j-api-2.20.0.jar
 */