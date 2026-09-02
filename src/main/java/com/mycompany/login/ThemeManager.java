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

public class ThemeManager {

    public enum Mode { LIGHT_PURPLE, DARK }

    private static final Path FILE = AppPaths.privateDataDir().resolve("theme_settings.json");
    private static final Path TEMP_FILE = AppPaths.privateDataDir().resolve("theme_settings.json.tmp");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final List<Runnable> listeners = new ArrayList<>();

    private static Mode mode = Mode.LIGHT_PURPLE;
    private static String customBackgroundPath = null;

    // Optional user-picked accent color that overrides the mode's built-in
    // accent everywhere (buttons, headers, table highlights, etc). Stored
    // as "#RRGGBB"; null means "use the mode default".
    private static Color customAccent = null;

    static {
        load();
    }

    private static class Settings {
        String mode;
        String customBackgroundPath;
        String customAccentHex;
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
                    if (s.customAccentHex != null) {
                        try {
                            customAccent = Color.decode(s.customAccentHex);
                        } catch (NumberFormatException ignored) {
                        }
                    }
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
            s.customAccentHex = (customAccent == null) ? null : toHex(customAccent);
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(TEMP_FILE), StandardCharsets.UTF_8)) {
                gson.toJson(s, writer);
                writer.flush();
            }
            Files.move(TEMP_FILE, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
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

    public static void setCustomBackground(Path sourceImage) throws IOException {
        String fileName = sourceImage.getFileName().toString();
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : ".png";
        Path dest = AppPaths.privateDataDir().resolve("custom_background" + ext);
        Files.copy(sourceImage, dest, StandardCopyOption.REPLACE_EXISTING);
        customBackgroundPath = dest.toAbsolutePath().toString();
        persist();
        notifyListeners();
    }

    public static void clearCustomBackground() {
        customBackgroundPath = null;
        persist();
        notifyListeners();
    }

    public static Path getCustomBackgroundPath() {
        return customBackgroundPath == null ? null : Path.of(customBackgroundPath);
    }

    /** Sets a user-picked accent color that overrides the mode's built-in accent everywhere. */
    public static void setCustomAccent(Color color) {
        customAccent = color;
        persist();
        notifyListeners();
    }

    /** Reverts to the mode's built-in accent color. */
    public static void clearCustomAccent() {
        customAccent = null;
        persist();
        notifyListeners();
    }

    public static boolean hasCustomAccent() {
        return customAccent != null;
    }

    /** The color currently shown as selected in a color picker, or null if using the mode default. */
    public static Color getCustomAccent() {
        return customAccent;
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
    // Light Purple: soft lavender accent on a muted lilac-gray background
    // (not stark white/bright, so long screen sessions are easier on the eyes).
    // Dark: the same lavender accent, brightened slightly, on near-black panels.
    // A custom accent (if set) overrides the lavender in both modes; the
    // page/card/text colors still follow light/dark since those exist for
    // contrast and readability, not branding.

    public static Color accent() {
        if (customAccent != null) {
            return customAccent;
        }
        return isDark() ? new Color(179, 157, 219) : new Color(142, 108, 200);
    }

    public static Color accentDark() {
        if (customAccent != null) {
            return shade(customAccent, -0.22f);
        }
        return isDark() ? new Color(142, 108, 200) : new Color(102, 72, 158);
    }

    public static Color pageBackground() {
        return isDark() ? new Color(24, 22, 30) : new Color(224, 216, 238);
    }

    public static Color cardBackground() {
        return isDark() ? new Color(38, 35, 47) : new Color(245, 241, 250);
    }

    public static Color cardBorder() {
        return isDark() ? new Color(66, 60, 80) : new Color(206, 196, 224);
    }

    public static Color textPrimary() {
        return isDark() ? new Color(235, 232, 240) : new Color(35, 30, 46);
    }

    public static Color textSecondary() {
        return isDark() ? new Color(175, 170, 185) : new Color(96, 88, 112);
    }

    /** amount &gt; 0 brightens, amount &lt; 0 darkens, preserving alpha. */
    private static Color shade(Color c, float amount) {
        int r = clamp((int) (c.getRed() + 255 * amount));
        int g = clamp((int) (c.getGreen() + 255 * amount));
        int b = clamp((int) (c.getBlue() + 255 * amount));
        return new Color(r, g, b, c.getAlpha());
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}