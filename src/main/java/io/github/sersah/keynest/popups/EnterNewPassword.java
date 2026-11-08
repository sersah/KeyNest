package io.github.sersah.keynest.popups;

import io.github.sersah.keynest.Dialogs;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Optional;


public class EnterNewPassword
{
    public static final int MIN_LENGTH = 8;

    /** Asks for a new master password (typed twice). Empty if the window was closed. */
    public static Optional<char[]> show()
    {
        Stage popupwindow = Dialogs.createStage("Create Database");

        Label label = new Label("This password is the only way to access your data.\nIt won't be saved, don't forget it!");
        label.setTextAlignment(TextAlignment.CENTER);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Master password");
        passwordField.setMaxWidth(250);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Repeat master password");
        confirmField.setMaxWidth(250);

        Label error = new Label();
        error.setStyle("-fx-text-fill: #ff6b6b;");

        char[][] result = new char[1][];

        Button button = new Button("Create database");
        button.setDefaultButton(true);
        button.setOnAction(e ->
        {
            String password = passwordField.getText();
            if (password.length() < MIN_LENGTH)
            {
                error.setText("Use at least " + MIN_LENGTH + " characters.");
            }
            else if (!password.equals(confirmField.getText()))
            {
                error.setText("The passwords don't match.");
            }
            else
            {
                result[0] = password.toCharArray();
                passwordField.clear();
                confirmField.clear();
                popupwindow.close();
            }
        });

        VBox layout = new VBox(7, label, passwordField, confirmField, error, button);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 340, 210);
        scene.getStylesheets().add(Dialogs.stylesheet());
        popupwindow.setScene(scene);
        popupwindow.setOnShown(e -> passwordField.requestFocus());
        popupwindow.showAndWait();

        return Optional.ofNullable(result[0]);
    }
}
