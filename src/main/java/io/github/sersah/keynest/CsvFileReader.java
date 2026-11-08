package io.github.sersah.keynest;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.crypto.AEADBadTagException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;


public class CsvFileReader
{
    //CSV file header
    private static final String[] FILE_HEADER_MAPPING = {"name", "email", "password"};

    //Account attributes
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";

    private static String decryptedDB = System.getProperty("user.home") + System.getProperty("file.separator") + ".keyholder";
    public static File decryptedFile = new File(decryptedDB);

    /**
     * Decrypts the vault and parses its accounts.
     *
     * @throws WrongPasswordException if the master password is wrong (or the file was tampered with)
     * @throws IOException            if the vault file cannot be read or is not a valid vault
     */
    public static ObservableList<Account> readCsvFile() throws WrongPasswordException, IOException
    {
        CSVFormat csvFileFormat = Builder.create().setHeader(FILE_HEADER_MAPPING).build();
        ObservableList<Account> accounts = FXCollections.observableArrayList();

        try
        {
            String decryptedPath = Encryption.decrypt();

            try (FileReader fileReader = new FileReader(decryptedPath);
                 CSVParser csvFileParser = new CSVParser(fileReader, csvFileFormat))
            {
                List<CSVRecord> csvRecords = csvFileParser.getRecords();

                //start from the second record to skip the header
                for (int i = 1; i < csvRecords.size(); i++)
                {
                    CSVRecord record = csvRecords.get(i);
                    accounts.add(new Account(record.get(NAME), record.get(EMAIL), record.get(PASSWORD)));
                }
            }
        }
        catch (AEADBadTagException e)
        {
            throw new WrongPasswordException(e);
        }
        catch (GeneralSecurityException | IllegalArgumentException e)
        {
            // IllegalArgumentException: file is not valid Base64, so it's not a KeyNest vault
            throw new IOException("Vault file is corrupted or in an unsupported format", e);
        }
        finally
        {
            //never leave the decrypted vault lying around, even if parsing failed
            decryptedFile.delete();
        }
        return accounts;
    }
}
