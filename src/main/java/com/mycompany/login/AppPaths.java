package com.mycompany.login;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Central place for where the app stores its files, so data doesn't
 * clutter whatever folder Asys.exe sits in.
 *
 * - privateDataDir(): hidden per-user folder for internal JSON files
 *   (accounts, attendance records, logs). Nobody needs to open this by hand.
 * - desktopRecordsDir(): one visible folder on the Desktop holding the
 *   Excel attendance mirror and the QR code images -- the things a
 *   teacher/admin actually wants to find and open while using the app.
 */
public class AppPaths {

    private static final String APP_FOLDER_NAME = "Asys";
    private static final String DESKTOP_FOLDER_NAME = "Asys Attendance Records";

    /** Hidden per-user folder for internal data files (users.json, attendance.json, etc). */
    public static Path privateDataDir() {
        Path dir = resolvePrivateBase().resolve(APP_FOLDER_NAME);
        createIfMissing(dir);
        return dir;
    }

    /** Visible folder on the Desktop for the Excel log and QR code images. */
    public static Path desktopRecordsDir() {
        Path dir = resolveDesktop().resolve(DESKTOP_FOLDER_NAME);
        createIfMissing(dir);
        return dir;
    }

    private static Path resolvePrivateBase() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Paths.get(appData);
        }
        // Non-Windows fallback: a hidden dot-folder in the user's home directory.
        return Paths.get(System.getProperty("user.home"), ".asys-data");
    }

    private static Path resolveDesktop() {
        Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
        if (Files.exists(desktop)) {
            return desktop;
        }
        return Paths.get(System.getProperty("user.home"));
    }

    private static void createIfMissing(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}