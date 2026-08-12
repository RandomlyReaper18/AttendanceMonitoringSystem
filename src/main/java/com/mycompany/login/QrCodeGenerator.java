package com.mycompany.login;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generates QR codes for student login. Each QR encodes "username:password"
 * as plain text -- scanning it is functionally equivalent to a physical ID
 * card (no typing needed). Since passwords are hashed in storage, a QR can
 * only be generated at the moment the plaintext password is actually known
 * (account creation, or a password reset) -- it can never be regenerated
 * later from a stored hash. The saved PNG is the durable copy.
 */
public class QrCodeGenerator {

    private static final int SIZE = 300; // pixels, square

    public static BufferedImage generate(String text) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, SIZE, SIZE);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public static String encodeCredentials(String username, String password) {
        return username + ":" + password;
    }

    /** Folder where QR PNGs are saved, next to the running app. Created if missing. */
    public static Path resolveQrFolder() {
        try {
            String jarDir = new File(QrCodeGenerator.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile()
                    .getAbsolutePath();
            Path folder = Paths.get(jarDir, "qr_codes");
            Files.createDirectories(folder);
            return folder;
        } catch (Exception e) {
            e.printStackTrace();
            Path fallback = Paths.get("qr_codes");
            try {
                Files.createDirectories(fallback);
            } catch (IOException ignored) {
            }
            return fallback;
        }
    }

    /** Generates and saves a student's QR code as qr_codes/&lt;username&gt;.png. Returns the saved path. */
    public static Path saveForStudent(String username, String password) throws IOException {
        BufferedImage image = generate(encodeCredentials(username, password));
        Path outputPath = resolveQrFolder().resolve(username + ".png");
        ImageIO.write(image, "PNG", outputPath.toFile());
        return outputPath;
    }

    public static boolean hasQrCode(String username) {
        return Files.exists(resolveQrFolder().resolve(username + ".png"));
    }

    public static Path getQrPath(String username) {
        return resolveQrFolder().resolve(username + ".png");
    }
}