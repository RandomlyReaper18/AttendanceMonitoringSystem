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
    private static final Path ADMIN_FILE = resolveDataFile("admin.json");
    private static final Path ADMIN_TEMP_FILE = resolveDataFile("admin.json.tmp");
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Data model for Admin Credentials
    public static class AdminAccount {
        private String username;
        private String passwordHash;

        public AdminAccount(String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
        }

        public String getUsername() { return username; }
        public String getPasswordHash() { return passwordHash; }
        public void setUsername(String username) { this.username = username; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    }

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

    /** Loads admin details or creates default credentials if admin.json does not exist. */
    public static synchronized AdminAccount loadAdmin() {
        try {
            if (!Files.exists(ADMIN_FILE)) {
                // Default credentials created on first launch
                AdminAccount defaultAdmin = new AdminAccount("admin", PasswordHasher.hash("admin123"));
                saveAdmin(defaultAdmin);
                return defaultAdmin;
            }
            try (Reader reader = new InputStreamReader(
                    Files.newInputStream(ADMIN_FILE), StandardCharsets.UTF_8)) {
                AdminAccount admin = gson.fromJson(reader, AdminAccount.class);
                return (admin != null) ? admin : new AdminAccount("admin", PasswordHasher.hash("admin123"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AdminAccount("admin", PasswordHasher.hash("admin123"));
        }
    }

    /** Saves updated admin details atomically. */
    public static synchronized void saveAdmin(AdminAccount admin) {
        try {
            try (Writer writer = new OutputStreamWriter(
                    Files.newOutputStream(ADMIN_TEMP_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(admin, writer);
                writer.flush();
            }
            Files.move(ADMIN_TEMP_FILE, ADMIN_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to save admin data to: " + ADMIN_FILE.toAbsolutePath());
        }
    }

    /** Verifies admin login using hashed password stored in admin.json. */
    public static boolean adminLogin(String username, String password) {
        AdminAccount admin = loadAdmin();
        return admin.getUsername().equals(username) 
                && PasswordHasher.verify(password, admin.getPasswordHash());
    }

    /** Call this method from your Admin Panel UI whenever you want to change admin credentials. */
    public static synchronized boolean updateAdminCredentials(String newUsername, String newPassword) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return false;
        }

        AdminAccount admin = loadAdmin();
        admin.setUsername(newUsername.trim());

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            admin.setPasswordHash(PasswordHasher.hash(newPassword));
        }

        saveAdmin(admin);
        return true;
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

    public static boolean register(String username, String password, String name) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        users.add(new User(username, PasswordHasher.hash(password), name));
        saveUsers(users);
        return true;
    }

    public static boolean login(String username, String password) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)
                    && PasswordHasher.verify(password, u.getPassword())) {
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean deleteUser(String username) {
        ArrayList<User> users = loadUsers();
        boolean removed = users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        if (removed) {
            saveUsers(users);
        }
        return removed;
    }

    public static synchronized boolean updateUser(String oldUsername, String newUsername, String newPassword, String newName) {
        ArrayList<User> users = loadUsers();

        int index = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(oldUsername)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return false;
        }

        if (!oldUsername.equalsIgnoreCase(newUsername)) {
            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(newUsername)) {
                    return false;
                }
            }
        }

        String passwordToStore = (newPassword == null || newPassword.isEmpty())
                ? users.get(index).getPassword()
                : PasswordHasher.hash(newPassword);

        users.set(index, new User(newUsername, passwordToStore, newName));
        saveUsers(users);
        return true;
    }

    public static boolean isStrongPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasNumber = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasNumber = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasNumber && hasSpecial;
    }
}