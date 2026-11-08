package io.github.sersah.keynest;

/**
 * Thrown when the vault cannot be decrypted with the given master password.
 * AES-GCM detects this through its authentication tag, so a wrong password
 * never silently produces an empty or garbled vault.
 */
public class WrongPasswordException extends Exception
{
    public WrongPasswordException(Throwable cause)
    {
        super("Wrong master password or tampered vault file", cause);
    }
}
