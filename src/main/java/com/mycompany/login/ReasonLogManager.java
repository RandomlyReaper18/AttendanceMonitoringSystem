package com.mycompany.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class ReasonLogManager {

    private static final Path FILE = resolveDataFile("reason_log.json");
    private static final Path TEMP_FILE = resolveDataFile("reason_log.json.tmp");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static Path resolveDataFile(String name) {
        try {
            String jarDir = new File(ReasonLogManager.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .getAbsolutePath();
            return Paths.get(jarDir, name);
        } catch (Exception e) {
            return Paths.get(name);
        }
    }

    public static synchronized ArrayList<LogEntry> loadEntries() {
        try {
            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }
            try (Reader reader = new InputStreamReader(
                    Files.newInputStream(FILE), StandardCharsets.UTF_8)) {
                Type type = new TypeToken<ArrayList<LogEntry>>() {}.getType();
                ArrayList<LogEntry> list = gson.fromJson(reader, type);
                return (list != null) ? list : new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load reason log from: " + FILE.toAbsolutePath());
            return new ArrayList<>();
        }
    }

    public static synchronized void saveEntries(ArrayList<LogEntry> entries) {
        try {
            try (Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(TEMP_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(entries, writer);
                writer.flush();
            }
            Files.move(TEMP_FILE, FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save reason log to: " + FILE.toAbsolutePath());
        }
    }

    public static synchronized void addEntry(LogEntry entry) {
        ArrayList<LogEntry> entries = loadEntries();
        entries.add(entry);
        saveEntries(entries);
    }

    /** Checks whether an answer of this type already exists for this user+date, to avoid re-prompting. */
    public static synchronized boolean hasEntry(String username, String date, String type) {
        for (LogEntry e : loadEntries()) {
            if (e.getUsername().equals(username) && e.getDate().equals(date) && e.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}