package Common;

import org.mindrot.jbcrypt.BCrypt;

public class HashUtil {
    
    // Hash a password using BCrypt
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }
    
    // Check if a plain text password matches a hashed password
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // In case of invalid hash format
            return false;
        }
    }
}
