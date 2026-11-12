package io.github.sersah.keynest.generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates random passwords. No JavaFX in here, so it can be unit tested.
 */
public class PasswordGenerator {

    public static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    public static final String DIGITS = "0123456789";
    public static final String SPECIAL = "!@#$%&*()-_=+[]{}|;:,.<>?/";

    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 99;

    public enum Strength { WEAK, MEDIUM, STRONG }

    private final SecureRandom random;

    public PasswordGenerator() {
        this(new SecureRandom());
    }

    PasswordGenerator(SecureRandom random) {
        this.random = random;
    }

    /**
     * Generates a password with at least one character from every selected set.
     *
     * @throws IllegalArgumentException if the length is out of range or no set is selected
     */
    public String generate(int length, boolean upper, boolean lower, boolean digits, boolean special) {
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException("Length must be between " + MIN_LENGTH + " and " + MAX_LENGTH);
        }
        List<String> sets = selectedSets(upper, lower, digits, special);
        if (sets.isEmpty()) {
            throw new IllegalArgumentException("Select at least one character set");
        }

        List<Character> chars = new ArrayList<>(length);

        // one from each selected set, so e.g. "digits" really gives at least one digit
        for (String set : sets) {
            chars.add(randomChar(set));
        }
        String all = String.join("", sets);
        while (chars.size() < length) {
            chars.add(randomChar(all));
        }
        // shuffle, otherwise the first characters would always be upper, lower, digit, ...
        Collections.shuffle(chars, random);

        StringBuilder password = new StringBuilder(length);
        chars.forEach(password::append);
        return password.toString();
    }

    /**
     * Entropy in bits: length * log2(size of the character pool).
     * This is what an attacker who knows the settings has to brute force.
     */
    public static double entropyBits(int length, boolean upper, boolean lower, boolean digits, boolean special) {
        int poolSize = String.join("", selectedSets(upper, lower, digits, special)).length();
        if (poolSize == 0) {
            return 0;
        }
        return length * (Math.log(poolSize) / Math.log(2));
    }

    public static Strength rate(double entropyBits) {
        if (entropyBits >= 80) {
            return Strength.STRONG;
        }
        if (entropyBits >= 50) {
            return Strength.MEDIUM;
        }
        return Strength.WEAK;
    }

    private char randomChar(String set) {
        return set.charAt(random.nextInt(set.length()));
    }

    private static List<String> selectedSets(boolean upper, boolean lower, boolean digits, boolean special) {
        List<String> sets = new ArrayList<>();
        if (upper) sets.add(UPPERCASE);
        if (lower) sets.add(LOWERCASE);
        if (digits) sets.add(DIGITS);
        if (special) sets.add(SPECIAL);
        return sets;
    }
}
