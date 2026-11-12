package io.github.sersah.keynest.vault;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the encrypted vault file.
 *
 * The decrypted content only exists in memory: the CSV is built in a
 * StringWriter, encrypted, and only the ciphertext is written to disk.
 */
public class VaultRepository
{
    private static final String[] HEADER = {"name", "email", "password"};

    // writing prints the header line
    private static final CSVFormat WRITE_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader(HEADER)
            .setRecordSeparator("\n")
            .get();

    // reading takes the column names from the first line. Careful: setSkipHeaderRecord
    // on the write format would also stop the printer from writing the header,
    // and then the first account would be read as the header and get lost.
    private static final CSVFormat READ_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .get();

    private final Path file;

    public VaultRepository(Path file)
    {
        this.file = file;
    }

    /** The vault in the user's home directory: ~/keyholder */
    public static VaultRepository defaultLocation()
    {
        return new VaultRepository(Path.of(System.getProperty("user.home"), "keyholder"));
    }

    public Path getFile()
    {
        return file;
    }

    public boolean exists()
    {
        return Files.isRegularFile(file);
    }

    /** Creates a new, empty vault and returns the key for it. */
    public VaultKey create(char[] masterPassword) throws IOException
    {
        if (Files.exists(file))
        {
            throw new FileAlreadyExistsException(file.toString());
        }
        VaultKey key = VaultCrypto.newKey(masterPassword);
        save(List.of(), key);
        return key;
    }

    /** Derives the key for this vault from the master password (does not check it yet). */
    public VaultKey deriveKey(char[] masterPassword) throws IOException
    {
        return VaultCrypto.keyFor(Files.readAllBytes(file), masterPassword);
    }

    public List<Account> load(VaultKey key) throws WrongPasswordException, IOException
    {
        String csv = VaultCrypto.decrypt(Files.readAllBytes(file), key);
        return fromCsv(csv);
    }

    /**
     * Encrypts and saves the accounts. Writes to a temporary file first and then
     * moves it over the vault, so a crash in the middle of saving can't corrupt it.
     */
    public void save(List<Account> accounts, VaultKey key) throws IOException
    {
        byte[] encrypted = VaultCrypto.encrypt(toCsv(accounts), key);

        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(tmp, encrypted);
        restrictToOwner(tmp);
        try
        {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException e)
        {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static String toCsv(List<Account> accounts)
    {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, WRITE_FORMAT))
        {
            for (Account account : accounts)
            {
                printer.printRecord(account.name(), account.email(), account.password());
            }
        }
        catch (IOException e)
        {
            // a StringWriter doesn't throw IOExceptions
            throw new UncheckedIOException(e);
        }
        return out.toString();
    }

    static List<Account> fromCsv(String csv) throws IOException
    {
        List<Account> accounts = new ArrayList<>();
        try (CSVParser parser = READ_FORMAT.parse(new StringReader(csv)))
        {
            for (CSVRecord record : parser)
            {
                accounts.add(new Account(record.get("name"), record.get("email"), record.get("password")));
            }
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            throw new IOException("Vault content is not valid", e);
        }
        return accounts;
    }

    // rw------- so other users on the same machine can't read the vault (only works on Linux/macOS)
    private static void restrictToOwner(Path path)
    {
        try
        {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
        }
        catch (UnsupportedOperationException | IOException ignored)
        {
            // Windows: no POSIX permissions, the home directory is private anyway
        }
    }
}
