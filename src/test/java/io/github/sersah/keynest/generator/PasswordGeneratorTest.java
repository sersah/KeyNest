package io.github.sersah.keynest.generator;

import io.github.sersah.keynest.generator.PasswordGenerator.Strength;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest
{
    private final PasswordGenerator generator = new PasswordGenerator();

    @ParameterizedTest
    @ValueSource(ints = {4, 12, 50, 99})
    void passwordHasRequestedLength(int length)
    {
        assertEquals(length, generator.generate(length, true, true, true, true).length());
    }

    @Test
    void onlyDigitsWhenOnlyDigitsSelected()
    {
        String password = generator.generate(30, false, false, true, false);

        assertTrue(password.chars().allMatch(Character::isDigit), password);
    }

    @RepeatedTest(50)
    void containsAtLeastOneCharacterOfEverySelectedSet()
    {
        // with length 4 and 4 sets, every set must appear exactly once
        String password = generator.generate(4, true, true, true, true);

        assertTrue(containsAny(password, PasswordGenerator.UPPERCASE), password);
        assertTrue(containsAny(password, PasswordGenerator.LOWERCASE), password);
        assertTrue(containsAny(password, PasswordGenerator.DIGITS), password);
        assertTrue(containsAny(password, PasswordGenerator.SPECIAL), password);
    }

    @Test
    void passwordsAreNotRepeated()
    {
        assertNotEquals(generator.generate(20, true, true, true, true),
                generator.generate(20, true, true, true, true));
    }

    @Test
    void rejectsInvalidInput()
    {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(3, true, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(100, true, true, true, true));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(12, false, false, false, false));
    }

    @Test
    void entropyIsLengthTimesLog2OfPoolSize()
    {
        // 10 digits -> log2(10) ~ 3.32 bits per character
        assertEquals(10 * Math.log(10) / Math.log(2), PasswordGenerator.entropyBits(10, false, false, true, false), 1e-9);
        assertEquals(0, PasswordGenerator.entropyBits(10, false, false, false, false));
    }

    @Test
    void strengthRating()
    {
        // 6 digits: ~20 bits
        assertEquals(Strength.WEAK, PasswordGenerator.rate(PasswordGenerator.entropyBits(6, false, false, true, false)));
        // 10 characters, all sets (88 chars): ~65 bits
        assertEquals(Strength.MEDIUM, PasswordGenerator.rate(PasswordGenerator.entropyBits(10, true, true, true, true)));
        // 16 characters, all sets: ~103 bits
        assertEquals(Strength.STRONG, PasswordGenerator.rate(PasswordGenerator.entropyBits(16, true, true, true, true)));
    }

    private static boolean containsAny(String password, String set)
    {
        return password.chars().anyMatch(c -> set.indexOf(c) >= 0);
    }
}
