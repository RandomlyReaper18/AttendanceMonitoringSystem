package com.mycompany.login;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted PBKDF2 password hashing, using only standard JDK classes (no
 * extra dependencies needed). Stores as "base64(salt):base64(hash)".
 */
public class PasswordHasher {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Hashes a plain-text password into a storable "salt:hash" string. */
    public static String hash(String plainPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /** Checks a plain-text password against a previously stored "salt:hash" string. */
    public static boolean verify(String plainPassword, String stored) {
        try {
            String[] parts = stored.split(":");
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual = pbkdf2(plainPassword.toCharArray(), salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}