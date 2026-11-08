package io.github.sersah.keynest;

import io.github.sersah.keynest.popups.EnterNewPassword;
import io.github.sersah.keynest.popups.EnterPassword;
import io.github.sersah.keynest.vault.Account;
import io.github.sersah.keynest.vault.VaultKey;
import io.github.sersah.keynest.vault.VaultRepository;
import io.github.sersah.keynest.vault.VaultSession;
import io.github.sersah.keynest.vault.WrongPasswordException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LoginController
{
    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    private final VaultRepository repository = VaultRepository.defaultLocation();

    @FXML
    private Label isDatabaseCreated;

    @FXML
    private void createDatabase()
    {
        if (repository.exists())
        {
            Dialogs.info("Database found", "An existing database has been found.\nYou can't create another one");
            return;
        }

        Optional<char[]> password = EnterNewPassword.show();
        if (password.isEmpty())
        {
            return;
        }

        try
        {
            repository.create(password.get());
            isDatabaseCreated.setText("Database created in\n" + repository.getFile());
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Could not create vault", e);
            isDatabaseCreated.setText("The database could not be created.\n" + e.getMessage());
        }
        finally
        {
            Arrays.fill(password.get(), '\0');
        }
    }

    @FXML
    private void loadDatabase() throws IOException
    {
        if (!repository.exists())
        {
            Dialogs.info("No database found", "No existing database has been found.\nCreate one now!");
            return;
        }

        Optional<char[]> password = EnterPassword.show();
        if (password.isEmpty())
        {
            return;
        }

        // decrypt first and only open the vault view if that worked,
        // otherwise a wrong password would show (and later save) an empty vault
        VaultSession session;
        try
        {
            VaultKey key = repository.deriveKey(password.get());
            List<Account> accounts = repository.load(key);
            session = new VaultSession(repository, key, accounts);
        }
        catch (WrongPasswordException e)
        {
            isDatabaseCreated.setText("The password is incorrect.\nTry again!");
            return;
        }
        catch (IOException e)
        {
            LOG.log(Level.WARNING, "Could not read vault", e);
            isDatabaseCreated.setText("The database could not be read.\n" + e.getMessage());
            return;
        }
        finally
        {
            // the derived key is all we need from now on
            Arrays.fill(password.get(), '\0');
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUImain.fxml"));
        Parent root = loader.load();
        GeneratorController controller = loader.getController();
        controller.switchKeyManager(session);

        App.stage.setScene(new Scene(root));
        App.stage.centerOnScreen();
    }
}
