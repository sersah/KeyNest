package io.github.sersah.keynest;

import io.github.sersah.keynest.generator.PasswordGenerator;
import io.github.sersah.keynest.vault.VaultSession;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.io.IOException;

public class GeneratorController {

    // clipboard is cleared after this time, so the password doesn't stay there forever
    private static final Duration CLIPBOARD_CLEAR_DELAY = Duration.seconds(30);

    private final PasswordGenerator generator = new PasswordGenerator();
    private String generatedPassword = "";

    private Timeline timeline; // animation

    @FXML
    private CheckBox checkboxLowercase;

    @FXML
    private CheckBox checkboxNumbers;

    @FXML
    private CheckBox checkboxSpecialCharacters;

    @FXML
    private CheckBox checkboxUppercase;

    @FXML
    private Label labelPassword;

    @FXML
    private Label labelStrength;

    @FXML
    private TextField textfield;

    @FXML
    private ProgressBar barstrength;

    @FXML
    private ToggleButton toggleKM;

    @FXML
    public BorderPane borderpane;

    
    private void showPopup(String message, TextField textField) {
        Tooltip popup = new Tooltip(message);
        popup.setOpacity(0.77);

        popup.setAutoHide(true);
        double x = textField.getScene().getWindow().getX() + textField.getLayoutX() + textField.getWidth() + 30; //center
        double y = textField.getScene().getWindow().getY() + textField.getLayoutY() + textField.getHeight() + 160; //center
        popup.show(textField, x, y);

        PauseTransition delay = new PauseTransition(Duration.seconds(2)); // Show duration
        delay.setOnFinished(e -> popup.hide());
        delay.play();
    }

    @FXML
    void buttonCopy(ActionEvent event) {
        if (generatedPassword.isEmpty()) {
            labelPassword.setText("Generate a password first!");
            return;
        }
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(generatedPassword);
        clipboard.setContent(content);
        showPopup("Copied! Clipboard is cleared in 30 seconds.", textfield);

        String copied = generatedPassword;
        PauseTransition clear = new PauseTransition(CLIPBOARD_CLEAR_DELAY);
        clear.setOnFinished(e -> {
            // only clear it if the user hasn't copied something else in the meantime
            if (copied.equals(clipboard.getString())) {
                clipboard.clear();
            }
        });
        clear.play();
    }

    @FXML
    void buttonGenerate(ActionEvent event) {
        int length;
        try {
            length = Integer.parseInt(textfield.getText().strip());
        } catch (NumberFormatException e) {
            showError("Please enter a valid number!");
            return;
        }

        boolean upper = checkboxUppercase.isSelected();
        boolean lower = checkboxLowercase.isSelected();
        boolean digits = checkboxNumbers.isSelected();
        boolean special = checkboxSpecialCharacters.isSelected();

        if (length < PasswordGenerator.MIN_LENGTH) {
            showError("Password has to be at least " + PasswordGenerator.MIN_LENGTH + " characters long!");
            return;
        }
        if (length > PasswordGenerator.MAX_LENGTH) {
            showError("Password can only be " + PasswordGenerator.MAX_LENGTH + " characters long!");
            return;
        }
        if (!upper && !lower && !digits && !special) {
            showError("Select at least one option!");
            return;
        }

        generatedPassword = generator.generate(length, upper, lower, digits, special);
        animatePassword();
        updatePasswordStrength(PasswordGenerator.entropyBits(length, upper, lower, digits, special));
    }

    private void showError(String message) {
        labelPassword.setText(message);
        labelStrength.setText("");
    }

    // types the password character by character
    private void animatePassword() {
        if (timeline != null) {
            timeline.stop();
        }
        labelPassword.setText("");
        timeline = new Timeline();
        for (int i = 0; i < generatedPassword.length(); i++) {
            int index = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(20 * i),
                    event -> labelPassword.setText(generatedPassword.substring(0, index + 1))));
        }
        timeline.play();
    }

    private void updatePasswordStrength(double entropyBits) {
        String strength;
        switch (PasswordGenerator.rate(entropyBits)) {
            case STRONG -> {
                strength = "Strong";
                barstrength.setProgress(1.0);
                barstrength.setStyle("-fx-accent: #1dc412;");
            }
            case MEDIUM -> {
                strength = "Medium";
                barstrength.setProgress(0.5);
                barstrength.setStyle("-fx-accent: #ffae10;");
            }
            default -> {
                strength = "Weak";
                barstrength.setProgress(0.25);
                barstrength.setStyle("-fx-accent: red;");
            }
        }
        labelStrength.setText(strength + " Password (~" + Math.round(entropyBits) + " bits)");
    }

    // scene changes:
    public void switchKeyGenerator(ActionEvent event) throws IOException {
        AnchorPane view = FXMLLoader.load(getClass().getResource("KeyGenerator.fxml"));
        borderpane.setCenter(view);
    }
    
    public void switchKeyManagerLogin(ActionEvent event) throws IOException {
        AnchorPane view = FXMLLoader.load(getClass().getResource("KeyManagerLogin.fxml"));
        borderpane.setCenter(view);
    }
    
    public void switchKeyManager(VaultSession session) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("KeyManager.fxml"));
        AnchorPane view = loader.load();
        DatabaseController databaseController = loader.getController();
        databaseController.setSession(session);
        borderpane.setCenter(view);
        toggleKM.setSelected(true); // needed as without it, every button becomes the Generator-Button
    }
}

