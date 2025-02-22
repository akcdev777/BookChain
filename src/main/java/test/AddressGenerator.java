package test;

import java.security.SecureRandom;

public class AddressGenerator {

    private static final String HEX_CHARS = "0123456789abcdef";

    public static String generateRandomAddress() {
        SecureRandom random = new SecureRandom(); // Create a fresh SecureRandom instance
        StringBuilder address = new StringBuilder("0x");
        for (int i = 0; i < 40; i++) {
            int index = random.nextInt(HEX_CHARS.length());
            address.append(HEX_CHARS.charAt(index));
        }
        return address.toString();
    }
}
