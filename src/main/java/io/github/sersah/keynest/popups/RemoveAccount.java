package io.github.sersah.keynest.popups;

import io.github.sersah.keynest.Dialogs;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;


public class RemoveAccount
{
    /** Asks for confirmation. Returns true only if the user clicked "Remove". */
    public static boolean confirm(String accountName)
    {
        Stage popupwindow = Dialogs.createStage("Remove account");

        Label label = new Label("Are you sure you want to remove\n\"" + accountName + "\"?");
        label.setTextAlignment(TextAlignment.CENTER);

        boolean[] confirmed = {false};

        Button remove = new Button("Remove");
        remove.setOnAction(e ->
        {
            confirmed[0] = true;
            popupwindow.close();
        });

        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);
        cancel.setDefaultButton(true); // Enter shouldn't delete by accident
        cancel.setOnAction(e -> popupwindow.close());

        HBox buttons = new HBox(10, cancel, remove);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(12, label, buttons);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 120);
        scene.getStylesheets().add(Dialogs.stylesheet());
        popupwindow.setScene(scene);
        popupwindow.showAndWait();

        return confirmed[0];
    }
}
