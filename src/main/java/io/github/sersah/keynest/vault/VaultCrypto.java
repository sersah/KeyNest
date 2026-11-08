package io.github.sersah.keynest.vault;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Encrypts and decrypts the vault file.
 *
 * File layout: MAGIC (4) | VERSION (1) | salt (16) | iv (12) | ciphertext + GCM tag (16)
 *
 * The key is derived from the master password with PBKDF2-HMAC-SHA256 and the
 * content is encrypted with AES-256-GCM. The header (magic, version, salt) is
 * passed as additional authenticated data, so changing it also breaks the tag.
 */
public final class VaultCrypto
{
    private static final byte[] MAGIC = {'K', 'N', 'S', 'T'};
    private static final byte VERSION = 1;

    static final int SALT_LENGTH = 16;
    static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int HEADER_LENGTH = MAGIC.length + 1 + SALT_LENGTH;

    // OWASP recommendation for PBKDF2-HMAC-SHA256 (2023)
    private static final int PBKDF2_ITERATIONS = 600_000;
    private static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private VaultCrypto()
    {
    }

    /** Key for a new vault, with a fresh random salt. */
    public static VaultKey newKey(char[] masterPassword)
    {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return new VaultKey(deriveKey(masterPassword, salt), salt);
    }

    /** Key for an existing vault. The salt is read from the file header. */
    public static VaultKey keyFor(byte[] vaultFile, char[] masterPassword) throws IOException
    {
        checkHeader(vaultFile);
        byte[] salt = Arrays.copyOfRange(vaultFile, MAGIC.length + 1, HEADER_LENGTH);
        return new VaultKey(deriveKey(masterPassword, salt), salt);
    }

    public static byte[] encrypt(String plaintext, VaultKey key)
    {
        byte[] iv = new byte[IV_LENGTH];
        RANDOM.nextBytes(iv); // new IV for every save, never reuse an IV with the same key

        ByteBuffer header = ByteBuffer.allocate(HEADER_LENGTH);
        header.put(MAGIC).put(VERSION).put(key.salt());

        try
        {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key.secretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            cipher.updateAAD(header.array());
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return ByteBuffer.allocate(HEADER_LENGTH + IV_LENGTH + cipherText.length)
                    .put(header.array())
                    .put(iv)
                    .put(cipherText)
                    .array();
        }
        catch (GeneralSecurityException e)
        {
            // AES-GCM is available on every Java platform, so this should never happen
            throw new IllegalStateException("AES-GCM not available", e);
        }
    }

    public static String decrypt(byte[] vaultFile, VaultKey key) throws WrongPasswordException, IOException
    {
        checkHeader(vaultFile);
        if (vaultFile.length < HEADER_LENGTH + IV_LENGTH + TAG_LENGTH_BITS / 8)
        {
            throw new IOException("Vault file is too short");
        }

        byte[] header = Arrays.copyOfRange(vaultFile, 0, HEADER_LENGTH);
        byte[] iv = Arrays.copyOfRange(vaultFile, HEADER_LENGTH, HEADER_LENGTH + IV_LENGTH);
        byte[] cipherText = Arrays.copyOfRange(vaultFile, HEADER_LENGTH + IV_LENGTH, vaultFile.length);

        try
        {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key.secretKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            cipher.updateAAD(header);
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        }
        catch (AEADBadTagException e)
        {
            // tag check failed: wrong password or the file was modified
            throw new WrongPasswordException(e);
        }
        catch (GeneralSecurityException e)
        {
            throw new IOException("Could not decrypt vault", e);
        }
    }

    private static void checkHeader(byte[] vaultFile) throws IOException
    {
        if (vaultFile.length < HEADER_LENGTH
                || !Arrays.equals(Arrays.copyOf(vaultFile, MAGIC.length), MAGIC))
        {
            throw new IOException("Not a KeyNest vault file");
        }
        if (vaultFile[MAGIC.length] != VERSION)
        {
            throw new IOException("Unsupported vault version " + vaultFile[MAGIC.length]);
        }
    }

    private static SecretKey deriveKey(char[] password, byte[] salt)
    {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        try
        {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        }
        catch (GeneralSecurityException e)
        {
            throw new IllegalStateException("PBKDF2 not available", e);
        }
        finally
        {
            spec.clearPassword();
        }
    }
}
