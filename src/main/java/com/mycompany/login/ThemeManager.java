package com.mycompany.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Central place for the app's color palette + the admin's chosen background
 * picture. Two modes: LIGHT_PURPLE (default) and DARK. Settings persist to
 * theme_settings.json so they survive a restart, and any screen can call
 * addListener(...) to get notified (and repaint itself) the instant the
 * theme or background picture changes elsewhere in the app.
 */
public class ThemeManager {

    public enum Mode { LIGHT_PURPLE, DARK }

    private static final Path FILE = AppPaths.privateDataDir().resolve("theme_settings.json");
    private static final Path TEMP_FILE = AppPaths.privateDataDir().resolve("theme_settings.json.tmp");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final List<Runnable> listeners = new ArrayList<>();

    private static Mode mode = Mode.LIGHT_PURPLE;
    private static String customBackgroundPath = null; // absolute path, or null = use default gradient

    static {
        load();
    }

    /** Small POJO just for JSON persistence. */
    private static class Settings {
        String mode;
        String customBackgroundPath;
    }

    private static void load() {
        try {
            if (!Files.exists(FILE)) {
                return;
            }
            try (Reader reader = new InputStreamReader(Files.newInputStream(FILE), StandardCharsets.UTF_8)) {
                Settings s = gson.fromJson(reader, Settings.class);
                if (s != null) {
                    if (s.mode != null) {
                        try {
                            mode = Mode.valueOf(s.mode);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    customBackgroundPath = s.customBackgroundPath;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static synchronized void persist() {
        try {
            Settings s = new Settings();
            s.mode = mode.name();
            s.customBackgroundPath = customBackgroundPath;
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(TEMP_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(s, writer);
                writer.flush();
            }
            Files.move(TEMP_FILE, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Mode getMode() {
        return mode;
    }

    public static void setMode(Mode newMode) {
        if (newMode == mode) {
            return;
        }
        mode = newMode;
        persist();
        notifyListeners();
    }

    public static boolean isDark() {
        return mode == Mode.DARK;
    }

    /** Copies the chosen image into the app's private data folder and remembers it as the background. */
    public static void setCustomBackground(Path sourceImage) throws IOException {
        String fileName = sourceImage.getFileName().toString();
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : ".png";
        Path dest = AppPaths.privateDataDir().resolve("custom_background" + ext);
        Files.copy(sourceImage, dest, StandardCopyOption.REPLACE_EXISTING);
        customBackgroundPath = dest.toAbsolutePath().toString();
        persist();
        notifyListeners();
    }

    /** Reverts to the default theme-colored gradient (no picture). */
    public static void clearCustomBackground() {
        customBackgroundPath = null;
        persist();
        notifyListeners();
    }

    /** Null if no custom background has been set. */
    public static Path getCustomBackgroundPath() {
        return customBackgroundPath == null ? null : Path.of(customBackgroundPath);
    }

    public static void addListener(Runnable onChange) {
        listeners.add(onChange);
    }

    public static void removeListener(Runnable onChange) {
        listeners.remove(onChange);
    }

    private static void notifyListeners() {
        for (Runnable r : new ArrayList<>(listeners)) {
            r.run();
        }
    }

    // ---------- Palette ----------
    // Light Purple: soft lavender accent on a very light lilac-white background.
    // Dark: the same lavender accent, brightened slightly, on near-black panels.

    public static Color accent() {
        return isDark() ? new Color(179, 157, 219) : new Color(149, 117, 205);
    }

    public static Color accentDark() {
        return isDark() ? new Color(149, 117, 205) : new Color(112, 80, 168);
    }

    public static Color pageBackground() {
        return isDark() ? new Color(28, 26, 34) : new Color(245, 242, 250);
    }

    public static Color cardBackground() {
        return isDark() ? new Color(42, 39, 51) : Color.WHITE;
    }

    public static Color cardBorder() {
        return isDark() ? new Color(70, 64, 84) : new Color(226, 220, 240);
    }

    public static Color textPrimary() {
        return isDark() ? new Color(235, 232, 240) : new Color(40, 35, 50);
    }

    public static Color textSecondary() {
        return isDark() ? new Color(175, 170, 185) : new Color(110, 102, 125);
    }
}