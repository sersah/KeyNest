package io.github.sersah.keynest.vault;

import javax.crypto.SecretKey;

/**
 * The AES key derived from the master password, plus the salt it was derived with.
 * The app keeps this while the vault is open instead of the master password itself.
 */
public record VaultKey(SecretKey secretKey, byte[] salt)
{
    public VaultKey
    {
        salt = salt.clone();
    }

    @Override
    public byte[] salt()
    {
        return salt.clone();
    }

    @Override
    public String toString()
    {
        return "VaultKey[***]";
    }
}
