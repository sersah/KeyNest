package io.github.sersah.keynest.popups;

import io.github.sersah.keynest.Dialogs;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;


public class EnterPassword
{
    /** Asks for the master password. Empty if the window was closed. */
    public static Optional<char[]> show()
    {
        Stage popupwindow = Dialogs.createStage("Enter password");

        Label label = new Label("Enter password to access database:");
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(250);

        char[][] result = new char[1][];

        Button button = new Button("Unlock");
        button.setDefaultButton(true);
        button.setOnAction(e ->
        {
            if (!passwordField.getText().isEmpty())
            {
                result[0] = passwordField.getText().toCharArray();
                passwordField.clear();
                popupwindow.close();
            }
        });

        VBox layout = new VBox(7, label, passwordField, new Region(), button);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 150);
        scene.getStylesheets().add(Dialogs.stylesheet());
        popupwindow.setScene(scene);
        popupwindow.showAndWait();

        return Optional.ofNullable(result[0]);
    }
}
