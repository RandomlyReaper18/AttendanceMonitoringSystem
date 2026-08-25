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
import java.util.LinkedHashSet;
import java.util.Set;

public class StudentSectionManager {

    private static final Path FILE = resolveDataFile("student_sections.json");
    private static final Path TEMP_FILE = resolveDataFile("student_sections.json.tmp");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static Path resolveDataFile(String name) {
        return AppPaths.privateDataDir().resolve(name);
    }

    public static synchronized ArrayList<StudentSection> loadAll() {
        try {
            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }
            try (Reader reader = new InputStreamReader(
                    Files.newInputStream(FILE), StandardCharsets.UTF_8)) {
                Type type = new TypeToken<ArrayList<StudentSection>>() {}.getType();
                ArrayList<StudentSection> list = gson.fromJson(reader, type);
                return (list != null) ? list : new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static synchronized void saveAll(ArrayList<StudentSection> list) {
        try {
            try (Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(TEMP_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(list, writer);
                writer.flush();
            }
            Files.move(TEMP_FILE, FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Records/updates which section+grade a student belongs to. */
    public static synchronized void assignSection(String username, String section, String grade) {
        ArrayList<StudentSection> list = loadAll();
        list.removeIf(s -> s.getUsername().equals(username));
        list.add(new StudentSection(username, section, grade));
        saveAll(list);
    }

    public static synchronized Set<String> getAllSectionNames() {
        Set<String> sections = new LinkedHashSet<>();
        for (StudentSection s : loadAll()) {
            sections.add(s.getSection());
        }
        return sections;
    }

    public static synchronized ArrayList<String> getUsernamesInSection(String section) {
        ArrayList<String> result = new ArrayList<>();
        for (StudentSection s : loadAll()) {
            if (s.getSection().equals(section)) {
                result.add(s.getUsername());
            }
        }
        return result;
    }
}