package io.github.sersah.keynest.vault;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultCryptoTest
{
    private static final char[] PASSWORD = "correct horse battery staple".toCharArray();

    @Test
    void decryptReturnsOriginalText()
    {
        VaultKey key = VaultCrypto.newKey(PASSWORD.clone());
        String text = "name,email,password\nGitHub,ich@example.de,Pässwörd🔑";

        assertEquals(text, assertDoesNotThrowDecrypt(VaultCrypto.encrypt(text, key), key));
    }

    @Test
    void sameKeyCanBeDerivedAgainFromTheFile() throws Exception
    {
        byte[] file = VaultCrypto.encrypt("secret", VaultCrypto.newKey(PASSWORD.clone()));

        VaultKey key = VaultCrypto.keyFor(file, PASSWORD.clone());

        assertEquals("secret", VaultCrypto.decrypt(file, key));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception
    {
        byte[] file = VaultCrypto.encrypt("secret", VaultCrypto.newKey(PASSWORD.clone()));

        VaultKey wrongKey = VaultCrypto.keyFor(file, "wrong password".toCharArray());

        assertThrows(WrongPasswordException.class, () -> VaultCrypto.decrypt(file, wrongKey));
    }

    @Test
    void modifiedCiphertextIsRejected()
    {
        VaultKey key = VaultCrypto.newKey(PASSWORD.clone());
        byte[] file = VaultCrypto.encrypt("secret", key);

        file[file.length - 1] ^= 1; // flip one bit

        assertThrows(WrongPasswordException.class, () -> VaultCrypto.decrypt(file, key));
    }

    @Test
    void modifiedSaltInHeaderIsRejected()
    {
        VaultKey key = VaultCrypto.newKey(PASSWORD.clone());
        byte[] file = VaultCrypto.encrypt("secret", key);

        file[5] ^= 1; // first salt byte, covered by the GCM tag as AAD

        assertThrows(WrongPasswordException.class, () -> VaultCrypto.decrypt(file, key));
    }

    @Test
    void sameTextEncryptsDifferentlyEveryTime()
    {
        VaultKey key = VaultCrypto.newKey(PASSWORD.clone());

        byte[] first = VaultCrypto.encrypt("secret", key);
        byte[] second = VaultCrypto.encrypt("secret", key);

        // random IV per save, so nobody can tell whether the content changed
        assertFalse(java.util.Arrays.equals(first, second));
    }

    @Test
    void newVaultsGetDifferentSalts()
    {
        VaultKey a = VaultCrypto.newKey(PASSWORD.clone());
        VaultKey b = VaultCrypto.newKey(PASSWORD.clone());

        assertFalse(java.util.Arrays.equals(a.salt(), b.salt()));
    }

    @Test
    void fileWithoutHeaderIsNotAVault()
    {
        byte[] notAVault = "name,email,password\n".getBytes();

        assertThrows(IOException.class, () -> VaultCrypto.keyFor(notAVault, PASSWORD.clone()));
    }

    @Test
    void saltIsCopiedSoItCantBeChangedFromOutside()
    {
        byte[] salt = new byte[VaultCrypto.SALT_LENGTH];
        VaultKey key = new VaultKey(null, salt);

        salt[0] = 42;

        assertArrayEquals(new byte[VaultCrypto.SALT_LENGTH], key.salt());
    }

    private static String assertDoesNotThrowDecrypt(byte[] file, VaultKey key)
    {
        try
        {
            return VaultCrypto.decrypt(file, key);
        }
        catch (Exception e)
        {
            throw new AssertionError("decrypt failed", e);
        }
    }
}
