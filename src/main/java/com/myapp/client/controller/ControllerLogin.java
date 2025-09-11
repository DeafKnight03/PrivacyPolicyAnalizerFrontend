package com.myapp.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControllerLogin {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML private void handleLogin() {
        System.out.println("Login: " + usernameField.getText() + " " + passwordField.getText());

    }
}
