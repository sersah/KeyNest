package io.github.sersah.keynest.vault;

import java.io.IOException;
import java.util.List;

/**
 * An unlocked vault: where it is stored and the key to save it again.
 */
public class VaultSession
{
    private final VaultRepository repository;
    private final VaultKey key;
    private final List<Account> accounts;

    public VaultSession(VaultRepository repository, VaultKey key, List<Account> accounts)
    {
        this.repository = repository;
        this.key = key;
        this.accounts = accounts;
    }

    public List<Account> getAccounts()
    {
        return accounts;
    }

    public VaultRepository getRepository()
    {
        return repository;
    }

    public void save(List<Account> accounts) throws IOException
    {
        repository.save(accounts, key);
    }
}
