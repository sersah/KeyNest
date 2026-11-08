package io.github.sersah.keynest;

import io.github.sersah.keynest.popups.EnterNewPassword;
import io.github.sersah.keynest.popups.EnterPassword;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LoginController
{
    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    private String encryptedDB = System.getProperty("user.home") + System.getProperty("file.separator") + "keyholder";
    private String decryptedDB = System.getProperty("user.home") + System.getProperty("file.separator") + ".keyholder";

    @FXML
    private Label isDatabaseCreated;

    @FXML
    private void createDatabase() throws IOException
    {
        File f = new File(encryptedDB);
        if (f.exists() && !f.isDirectory())
        {
            Dialogs.info("Database found", "An existing database has been found.\nYou can't create another one");
        }
        else
        {   //create new empty database
            EnterNewPassword popup = new EnterNewPassword();
            popup.display();

            if (EnterNewPassword.isPasswordIn)
            {
                CsvFileWriter.writeCsvFile(decryptedDB, null);
                isDatabaseCreated.setText("Database created in\n" + encryptedDB);
            }
        }
    }

    @FXML
    private void loadDatabase() throws IOException
    {
        File f = new File(encryptedDB);
        if (!f.exists() || f.isDirectory())
        {
            Dialogs.info("No database found", "No existing database has been found.\nCreate one now!");
            return;
        }

        EnterPassword popup = new EnterPassword();
        popup.display();
        if (!EnterPassword.isPasswordIn)
        {
            return;
        }

        // Decrypt BEFORE opening the vault view. Previously the view was opened first and a
        // wrong password produced an empty list, which the next save wrote over the real vault.
        ObservableList<Account> accounts;
        try
        {
            accounts = CsvFileReader.readCsvFile();
        }
        catch (WrongPasswordException e)
        {
            Encryption.initialKey = null; // never keep a wrong key around for a later save
            isDatabaseCreated.setText("The password is incorrect.\nTry again!");
            return;
        }
        catch (IOException e)
        {
            Encryption.initialKey = null;
            LOG.log(Level.WARNING, "Could not read vault", e);
            isDatabaseCreated.setText("The database could not be read.\n" + e.getMessage());
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUImain.fxml"));
        Parent root = loader.load();
        GeneratorController controller = loader.getController();
        controller.switchKeyManager(accounts);

        App.stage.setScene(new Scene(root));
        App.stage.centerOnScreen();
    }
}
