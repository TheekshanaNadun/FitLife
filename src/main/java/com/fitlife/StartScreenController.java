package com.fitlife;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import com.fitlife.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StartScreenController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Button exitBtn;

    @FXML
    public void initialize() {
        loginBtn.setOnAction(e -> login());
        registerBtn.setOnAction(e -> System.out.println("Register clicked"));
        exitBtn.setOnAction(e -> System.exit(0));
    }
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if(username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username and password!");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Use backticks around `type` because it's a reserved keyword in MySQL
            String sql = "SELECT `id`, `username`, `password`, `type` FROM `Login` WHERE `username`=? AND `password`=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                // Safely get the 'type' column
                String type = rs.getString("type");

                // Load dashboard based on account type
                if(type.equalsIgnoreCase("admin")) {
                    loadScreen("/admin_dashboard.fxml");
                } else {
                    loadScreen("/member_dashboard.fxml");
                }

            } else {
                showAlert("Error", "Invalid username or password!");
            }

        } catch(Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Database connection failed!");
        }
    }


    private void loadScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginBtn.getScene().getWindow(); // get current window
            stage.setScene(scene);
        } catch(Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load next screen!");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
