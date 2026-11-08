package io.github.sersah.keynest;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Small modal dialogs that share the app's icon and popup stylesheet.
 */
public final class Dialogs
{
    private static final String POPUP_CSS = "/io/github/sersah/keynest/popups.css";

    private Dialogs()
    {
    }

    /** Shows a modal dialog with a text message and a "Close" button. */
    public static void info(String title, String message)
    {
        show(title, new Label(message), 275, 115);
    }

    /** Shows a modal dialog with custom content and a "Close" button. */
    public static void show(String title, Node content, double width, double height)
    {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        if (App.stage != null)
        {
            popup.initOwner(App.stage); // centers the dialog on the main window
        }
        popup.setTitle(title);
        popup.getIcons().add(new Image("icon.png"));
        popup.setResizable(false);

        Button close = new Button("Close");
        close.setOnAction(e -> popup.close());
        close.setDefaultButton(true);

        VBox layout = new VBox(7, content, close);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, width, height);
        scene.getStylesheets().add(stylesheet());
        popup.setScene(scene);
        popup.showAndWait();
    }

    /** URL of the popup stylesheet, for popups that build their own scene. */
    public static String stylesheet()
    {
        return Dialogs.class.getResource(POPUP_CSS).toExternalForm();
    }
}
