package io.github.sersah.keynest;

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
import java.security.SecureRandom;

public class GeneratorController {

    private String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String lowercase = "abcdefghijklmnopqrstuvwxyz";
    private String numbers = "0123456789";
    private String specialCharacters = "!@#$%&*()-_=+[]{}|;:,.<>?/";
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
        if (!generatedPassword.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(generatedPassword);
            clipboard.setContent(content);
            showPopup("Password has been copied!", textfield);
        } else {
            labelPassword.setText("Generate a password first!");
        }
    }

    @FXML
    void buttonGenerate(ActionEvent event) {
        String passwordLength = textfield.getText();
        try {
            int passwordLengthInt = Integer.parseInt(passwordLength);
            if (passwordLengthInt < 4) {
                labelPassword.setText("Password has to be at least 4 characters long!");
                labelStrength.setText("");
                return;
            } else if (passwordLengthInt > 99) {
                labelPassword.setText("Password can only be 99 characters long!");
                labelStrength.setText("");
                return;
            }
            String characterSet = generateCharacterSet(
                    checkboxUppercase.isSelected(),
                    checkboxLowercase.isSelected(),
                    checkboxNumbers.isSelected(),
                    checkboxSpecialCharacters.isSelected()
            );

            if (characterSet.isEmpty()) {
                labelPassword.setText("Select at least one option!");
                labelStrength.setText("");
                return;
            }

            generatedPassword = generateRandomPassword(passwordLengthInt, characterSet);
            updatePasswordStrength(passwordLengthInt);
        } catch (NumberFormatException e) { // invalid input
            labelPassword.setText("Please enter a valid number!");
        }
    }

    private String generateCharacterSet(boolean isUppercase, boolean isLowercase, boolean isNumbers, boolean isSpecialCharacters) {
        StringBuilder characterSet = new StringBuilder();
        if (isUppercase) {
            characterSet.append(uppercase);
        }
        
        if (isLowercase) {
            characterSet.append(lowercase);
        }
        
        if (isNumbers) {
            characterSet.append(numbers);
        }
        
        if (isSpecialCharacters) {
            characterSet.append(specialCharacters);
        }
        
        return characterSet.toString();
    }

    private String generateRandomPassword(int passwordLength, String characterSet) {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        labelPassword.setText("");

        if (timeline != null) {
            timeline.stop();
        }
        //animation
        timeline = new Timeline();
        for (int i = 0; i < passwordLength; i++) {
            password.append(characterSet.charAt(random.nextInt(characterSet.length())));

            int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(20 * i), event -> {
                labelPassword.setText(labelPassword.getText() + generatedPassword.charAt(index));
            });

            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
        
        return password.toString();
    }

    private void updatePasswordStrength(int passwordLength) {
        String strength;
        if (passwordLength >= 12) {
            strength = "Strong";
            barstrength.setProgress(1.0);
            barstrength.setStyle("-fx-accent: #1dc412;");
        } else if (passwordLength >= 8) {
            strength = "Medium";
            barstrength.setProgress(0.5);
            barstrength.setStyle("-fx-accent: #ffae10;");
        } else {
            strength = "Weak";
            barstrength.setProgress(0.25);
            barstrength.setStyle("-fx-accent: red;");
        }
        labelStrength.setText(strength + " Password");
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
    
    public void switchKeyManager(ActionEvent event) throws IOException {
        AnchorPane view = FXMLLoader.load(getClass().getResource("KeyManager.fxml"));
        borderpane.setCenter(view);
        toggleKM.setSelected(true); // needed as without it, every button becomes the Generator-Button
    }
}

