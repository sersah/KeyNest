package com.example.KeyNest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Encryption
{
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 256;

    private static String encryptedDB = System.getProperty("user.home") + System.getProperty("file.separator") + "keyholder";
    private static String decryptedDB = System.getProperty("user.home") + System.getProperty("file.separator") + ".keyholder";

    private static File encryptedFile = new File(encryptedDB);
    public static File decryptedFile = new File(decryptedDB);

    public static String initialKey;

    public static void encrypt() //case: createDatabase() and save()
    {
        //read decryptedFile (created by writeCsvFile()) and store it in decryptedContent
        String decryptedContent = "";

        try
        {
            decryptedContent = Files.readString(Paths.get(decryptedFile.getAbsolutePath()), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            System.err.println("Error reading decrypted file: " + e.getMessage());
        }

        //encrypt decryptedContent and store it in encryptedContent
        try
        {
            String encryptedContent = Encryption.cipherEncrypt(decryptedContent, initialKey);

            //write encryptedContent in encryptedFile
            try (FileOutputStream fos = new FileOutputStream(encryptedFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos))
            {
                byte[] bytes = encryptedContent.getBytes(StandardCharsets.UTF_8);
                bos.write(bytes);
            }
        }
        catch (GeneralSecurityException | IOException e)
        {
            System.err.println("Error encrypting database: " + e.getMessage());
        }
        finally
        {
            //delete decryptedFile
            decryptedFile.delete();
        }
    }

    public static String decrypt() throws IOException, GeneralSecurityException //case: loadDatabase().initialize()
    {
        //read encryptedFile and store it in encryptedContent
        String encryptedContent = Files.readString(Paths.get(encryptedFile.getAbsolutePath()), StandardCharsets.UTF_8);

        //decrypt encryptedContent (throws AEADBadTagException if master password is wrong)
        String decryptedContent = cipherDecrypt(encryptedContent, initialKey);

        //create temporary decryptedFile and write decryptedContent
        decryptedFile.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(decryptedFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos))
        {
            byte[] bytes = decryptedContent.getBytes(StandardCharsets.UTF_8);
            bos.write(bytes);
        }

        //return decryptedFile path
        return decryptedFile.getAbsolutePath();
    }

    public static SecretKeySpec setKey(final String myKey, final byte[] salt) throws GeneralSecurityException
    {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(myKey.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public static String cipherEncrypt(final String strToEncrypt, final String initialKey) throws GeneralSecurityException
    {
        SecureRandom random = new SecureRandom();

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        SecretKeySpec secretKey = setKey(initialKey, salt);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

        ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + cipherText.length);
        byteBuffer.put(salt);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    public static String cipherDecrypt(final String strToDecrypt, final String initialKey) throws GeneralSecurityException
    {
        byte[] decoded = Base64.getDecoder().decode(strToDecrypt.trim());
        if (decoded.length < SALT_LENGTH_BYTES + IV_LENGTH_BYTES)
        {
            throw new GeneralSecurityException("Invalid encrypted payload");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        byteBuffer.get(salt);

        byte[] iv = new byte[IV_LENGTH_BYTES];
        byteBuffer.get(iv);

        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        SecretKeySpec secretKey = setKey(initialKey, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }
}
