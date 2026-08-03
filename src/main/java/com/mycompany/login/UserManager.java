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

public class UserManager {

    private static final Path FILE = resolveDataFile("users.json");
    private static final Path TEMP_FILE = resolveDataFile("users.json.tmp");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static Path resolveDataFile(String name) {
        try {
            String jarDir = new File(UserManager.class
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

    public static synchronized ArrayList<User> loadUsers() {
        try {
            if (!Files.exists(FILE)) {
                return new ArrayList<>();
            }

            try (Reader reader = new InputStreamReader(
                    Files.newInputStream(FILE), StandardCharsets.UTF_8)) {

                Type type = new TypeToken<ArrayList<User>>() {}.getType();
                ArrayList<User> users = gson.fromJson(reader, type);
                return (users != null) ? users : new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load user data from: " + FILE.toAbsolutePath());
            return new ArrayList<>();
        }
    }

    public static synchronized void saveUsers(ArrayList<User> users) {
        try {
            try (Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(TEMP_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(users, writer);
                writer.flush();
            }

            Files.move(TEMP_FILE, FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save user data to: " + FILE.toAbsolutePath());
        }
    }

    public static boolean adminLogin(String username, String password) {
        return username.equals("admin")
                && password.equals("admin123");
    }

    public static boolean register(String username, String password, String name) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        users.add(new User(username, password, name));
        saveUsers(users);
        return true;
    }

    public static boolean login(String username, String password) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)
                    && u.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    /** Removes a registered user by username. Returns true if a user was removed. */
    public static synchronized boolean deleteUser(String username) {
        ArrayList<User> users = loadUsers();
        boolean removed = users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        if (removed) {
            saveUsers(users);
        }
        return removed;
    }
}