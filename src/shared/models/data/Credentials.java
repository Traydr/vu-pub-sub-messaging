package shared.models.data;

import shared.util.Styling;

import java.io.Serial;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Credentials implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private final String username;
    private final String passwordHash;

    public Credentials(String username, String password) throws RuntimeException {
        this.username = username;
        this.passwordHash = hash(password);
    }

    private String hash(String value) throws RuntimeException {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failure occurred");
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

}