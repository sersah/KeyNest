package io.github.sersah.keynest.vault;

/**
 * The vault could not be decrypted with the given master password.
 * AES-GCM notices this through its authentication tag, so a wrong password
 * never silently gives an empty or broken vault.
 */
public class WrongPasswordException extends Exception
{
    public WrongPasswordException(Throwable cause)
    {
        super("Wrong master password or the vault file was modified", cause);
    }
}
