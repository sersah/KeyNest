package io.github.sersah.keynest.vault;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VaultRepositoryTest
{
    private static final char[] PASSWORD = "master-password".toCharArray();

    @TempDir
    Path dir;

    private VaultRepository repository;

    @BeforeEach
    void setUp()
    {
        repository = new VaultRepository(dir.resolve("keyholder"));
    }

    @Test
    void newVaultIsEmpty() throws Exception
    {
        VaultKey key = repository.create(PASSWORD.clone());

        assertTrue(repository.exists());
        assertEquals(List.of(), repository.load(key));
    }

    @Test
    void savedAccountsCanBeLoadedAgain() throws Exception
    {
        VaultKey key = repository.create(PASSWORD.clone());
        List<Account> accounts = List.of(
                new Account("GitHub", "me@example.com", "pa,ss\"word"),
                new Account("Uni", "s12345", "line1\nline2"),
                new Account("Bänk", "ö@ü.de", "€uro🔑"));

        repository.save(accounts, key);

        // simulate restarting the app: derive the key again from the password
        VaultKey keyAfterRestart = repository.deriveKey(PASSWORD.clone());
        assertEquals(accounts, repository.load(keyAfterRestart));
    }

    @Test
    void wrongPasswordDoesNotChangeTheFile() throws Exception
    {
        VaultKey key = repository.create(PASSWORD.clone());
        repository.save(List.of(new Account("GitHub", "me", "secret")), key);
        byte[] before = Files.readAllBytes(repository.getFile());

        VaultKey wrongKey = repository.deriveKey("wrong".toCharArray());

        assertThrows(WrongPasswordException.class, () -> repository.load(wrongKey));
        assertArrayEquals(before, Files.readAllBytes(repository.getFile()));
    }

    @Test
    void plaintextNeverEndsUpOnDisk() throws Exception
    {
        VaultKey key = repository.create(PASSWORD.clone());
        repository.save(List.of(new Account("GitHub", "me@example.com", "SuperSecret123")), key);
        repository.load(key);

        // only the vault itself, no temp or decrypted files
        try (Stream<Path> files = Files.list(dir))
        {
            assertEquals(List.of(repository.getFile()), files.toList());
        }
        String raw = new String(Files.readAllBytes(repository.getFile()), StandardCharsets.ISO_8859_1);
        assertFalse(raw.contains("SuperSecret123"));
        assertFalse(raw.contains("GitHub"));
    }

    @Test
    void createDoesNotOverwriteExistingVault() throws Exception
    {
        repository.create(PASSWORD.clone());

        assertThrows(FileAlreadyExistsException.class, () -> repository.create("other".toCharArray()));
    }

    @Test
    void randomFileIsNotAcceptedAsVault() throws Exception
    {
        Files.writeString(repository.getFile(), "hello");

        assertThrows(IOException.class, () -> repository.deriveKey(PASSWORD.clone()));
    }

    @Test
    void csvRoundTripKeepsSpecialCharacters() throws Exception
    {
        List<Account> accounts = List.of(new Account("a,b", "\"quoted\"", "multi\nline"));

        assertEquals(accounts, VaultRepository.fromCsv(VaultRepository.toCsv(accounts)));
    }
}
