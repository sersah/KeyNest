package io.github.sersah.keynest;

import io.github.sersah.keynest.popups.EnterNewPassword;
import io.github.sersah.keynest.popups.EnterPassword;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


public class LoginController
{
    private Scene scene;

    private String encryptedDB = System.getProperty("user.home") + System.getProperty("file.separator") + "keyholder";
    private String decryptedDB = System.getProperty("user.home") + System.getProperty("file.separator") + ".keyholder";

    @FXML
    private CheckMenuItem showPasswords;

    @FXML
    private Label isDatabaseCreated;

    @FXML
    private Button save;

    @FXML
    private Button focus;

    @FXML
    private void createDatabase() throws IOException
    {
        File f = new File(encryptedDB);
        if(f.exists() && !f.isDirectory())
        {   
            System.out.println("Database found!");

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Database found");
            Image iconpopup = new Image("icon.png");
            popup.getIcons().add(iconpopup);

            VBox layout= new VBox(7);

            Label label = new Label("An existing database has been found.\nYou can't create another one");

            Button button = new Button("Close");
            button.setOnAction(e -> popup.close());

            layout.getChildren().addAll(label, button);
            layout.setAlignment(Pos.CENTER);

            Scene scene = new Scene(layout, 265, 115);
            scene.getStylesheets().add(getClass().getResource("/io/github/sersah/keynest/popups.css").toExternalForm());

            popup.setX(660);
            popup.setY(225);
            popup.setScene(scene);
            popup.showAndWait();
        }
        else
        {   //create new empty database
            EnterNewPassword popup = new EnterNewPassword();
            popup.display();

            if(EnterNewPassword.isPasswordIn)
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
            if(f.exists() && !f.isDirectory())
            {
                EnterPassword popup = new EnterPassword();
                popup.display();

                if(EnterPassword.isPasswordIn)

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
                System.out.println("No database found!");
                Stage popup = new Stage();
                popup.initModality(Modality.APPLICATION_MODAL);
                popup.setTitle("No Database found");
                Image iconpopup = new Image("icon.png");
                popup.getIcons().add(iconpopup);

                VBox layout= new VBox(7);

                Label label = new Label("No existing database has been found.\nCreate one now!");

                Button button = new Button("Close");
                button.setOnAction(e -> popup.close());

                layout.getChildren().addAll(label, button);
                layout.setAlignment(Pos.CENTER);

                Scene scene = new Scene(layout, 275, 115);
                scene.getStylesheets().add(getClass().getResource("/io/github/sersah/keynest/popups.css").toExternalForm());

                popup.setX(660);
                popup.setY(225);
                popup.setScene(scene);
                popup.showAndWait();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            isDatabaseCreated.setText("The password is incorrect.\nTry again!");
        }
    }

}
