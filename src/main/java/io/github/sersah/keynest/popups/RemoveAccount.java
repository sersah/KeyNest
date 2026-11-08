package io.github.sersah.keynest.popups;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.MalformedURLException;


public class RemoveAccount
{
    public static boolean removeRow = false;

    public void display()
    {
        Stage popupwindow=new Stage();

        popupwindow.initModality(Modality.APPLICATION_MODAL);
        popupwindow.setTitle("Remove account");

        Label label = new Label("Are you sure you want to remove \nthe selected account?");
        label.setTextAlignment(TextAlignment.CENTER);

        Region region = new Region();

        Button button = new Button("Remove account");

        button.setOnAction(e ->
        {
            removeRow = true;
            popupwindow.close();
        });

        VBox layout= new VBox(10);

        layout.getChildren().addAll(label, region, button);

        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 100);

        scene.getStylesheets().add(getClass().getResource("/io/github/sersah/keynest/popups.css").toExternalForm());

        popupwindow.setScene(scene);

        popupwindow.showAndWait();
    }

}

