package io.github.sersah.keynest;

import io.github.sersah.keynest.popups.EnterNewPassword;
import io.github.sersah.keynest.popups.EnterPassword;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;


public class LoginController
{
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
        try
        {
            File f = new File(encryptedDB);
            if (f.exists() && !f.isDirectory())
            {
                EnterPassword popup = new EnterPassword();
                popup.display();

                if (EnterPassword.isPasswordIn)
                {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("GUImain.fxml"));
                    Parent root = loader.load();
                    GeneratorController controller = loader.getController();
                    controller.switchKeyManager(null);

                    Scene scene = new Scene(root);
                    App.stage.setScene(scene);
                    App.stage.centerOnScreen();
                }
            }
            else
            {
                Dialogs.info("No database found", "No existing database has been found.\nCreate one now!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            isDatabaseCreated.setText("The password is incorrect.\nTry again!");
        }
    }
}
