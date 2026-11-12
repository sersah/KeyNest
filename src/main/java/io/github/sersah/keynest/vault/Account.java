package io.github.sersah.keynest.vault;

/**
 * One entry in the vault.
 */
public record Account(String name, String email, String password)
{
    @Override
    public String toString()
    {
        // never print the password, e.g. in logs or the debugger
        return "Account[name=" + name + ", email=" + email + "]";
    }
}
