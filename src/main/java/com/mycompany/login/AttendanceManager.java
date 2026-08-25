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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class AttendanceManager {

    // Stored next to the running application, not the JVM's arbitrary
    // working directory -- avoids "my data disappeared" bugs caused by
    // launching the app from different locations.
    private static final Path FILE = AppPaths.privateDataDir().resolve("attendance.json");
    private static final Path TEMP_FILE = resolveDataFile("attendance.json.tmp");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private static Path resolveDataFile(String name) {
        return AppPaths.privateDataDir().resolve(name);
    }

    public static synchronized ArrayList<Attendance> loadAttendance() {
        try {
            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }

            try (Reader reader = new InputStreamReader(
                    Files.newInputStream(FILE), StandardCharsets.UTF_8)) {

                Type type = new TypeToken<ArrayList<Attendance>>() {}.getType();
                ArrayList<Attendance> list = gson.fromJson(reader, type);
                if (list == null) {
                    list = new ArrayList<>();
                }

                ArrayList<Attendance> cleaned = dedupeAndSort(list);

                // If cleanup actually removed duplicates, persist the
                // cleaned version immediately so the fix is permanent.
                if (cleaned.size() != list.size()) {
                    saveAttendance(cleaned);
                }

                return cleaned;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load attendance data from: " + FILE.toAbsolutePath());
            return new ArrayList<>();
        }
    }

    public static synchronized void saveAttendance(ArrayList<Attendance> list) {
        try {
            ArrayList<Attendance> cleaned = dedupeAndSort(list);

            try (Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(TEMP_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(cleaned, writer);
                writer.flush();
            }

            Files.move(TEMP_FILE, FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save attendance data to: " + FILE.toAbsolutePath());
        }
    }

    /**
     * Removes duplicate records for the same username+date (keeping the
     * LAST one in the list, since that reflects the most recent
     * login/logout state), then sorts the result by date ascending and
     * username alphabetically, so the table is always organized.
     */
    private static ArrayList<Attendance> dedupeAndSort(ArrayList<Attendance> list) {
        // LinkedHashMap preserves insertion order; re-putting the same key
        // moves its value to the latest entry, effectively keeping the
        // last occurrence for each username+date pair.
        LinkedHashMap<String, Attendance> byKey = new LinkedHashMap<>();
        for (Attendance a : list) {
            String key = a.getUsername() + "|" + a.getDate();
            byKey.put(key, a);
        }

        ArrayList<Attendance> result = new ArrayList<>(byKey.values());

        result.sort(Comparator
                .comparing(AttendanceManager::parseDateSafe)
                .thenComparing(a -> a.getUsername() == null ? "" : a.getUsername().toLowerCase()));

        return result;
    }

    private static LocalDate parseDateSafe(Attendance a) {
        try {
            return LocalDate.parse(a.getDate(), DATE_FORMAT);
        } catch (Exception e) {
            // Unparseable/blank dates sort first rather than crashing.
            return LocalDate.MIN;
        }
    }
}