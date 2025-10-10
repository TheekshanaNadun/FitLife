package com.fitlife;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class StartScreenController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginBtn;

    @FXML
    private Button registerBtn;

    @FXML
    private Button exitBtn;

    @FXML
    public void initialize() {
        // Example: Add button actions here
        loginBtn.setOnAction(e -> System.out.println("Login clicked"));
        registerBtn.setOnAction(e -> System.out.println("Register clicked"));
        exitBtn.setOnAction(e -> System.exit(0));
    }
}
