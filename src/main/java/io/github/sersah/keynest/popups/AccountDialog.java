package io.github.sersah.keynest.popups;

import io.github.sersah.keynest.Dialogs;
import io.github.sersah.keynest.vault.Account;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Form for adding a new account or editing an existing one.
 */
public class AccountDialog
{
    public static Optional<Account> add()
    {
        return show("Add account", "Add account", new Account("", "", ""));
    }

    public static Optional<Account> edit(Account account)
    {
        return show("Edit account", "Save changes", account);
    }

    private static Optional<Account> show(String title, String buttonText, Account initial)
    {
        Stage popupwindow = Dialogs.createStage(title);

        TextField nameField = field(initial.name());
        TextField emailField = field(initial.email());
        TextField passwordField = field(initial.password());

        Label error = new Label();
        error.setStyle("-fx-text-fill: #ff6b6b;");

        // set only when the user confirms, closing the window with X leaves it empty
        Account[] result = new Account[1];

        Button button = new Button(buttonText);
        button.setDefaultButton(true);
        button.setOnAction(e ->
        {
            if (nameField.getText().isBlank() || emailField.getText().isBlank() || passwordField.getText().isBlank())
            {
                error.setText("Please fill in all fields.");
                return;
            }
            result[0] = new Account(nameField.getText().strip(), emailField.getText().strip(), passwordField.getText());
            popupwindow.close();
        });

        VBox layout = new VBox(7,
                new Label("Name:"), nameField,
                new Label("Email / username:"), emailField,
                new Label("Password:"), passwordField,
                error, new Region(), button);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 280);
        scene.getStylesheets().add(Dialogs.stylesheet());
        popupwindow.setScene(scene);
        popupwindow.showAndWait();

        return Optional.ofNullable(result[0]);
    }

    private static TextField field(String text)
    {
        TextField field = new TextField(text);
        field.setMaxWidth(250);
        return field;
    }
}
