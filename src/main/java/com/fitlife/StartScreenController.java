package com.fitlife;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
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
        // Attach button actions
        loginBtn.setOnAction(e -> login());
        registerBtn.setOnAction(e -> handleRegister());
        exitBtn.setOnAction(e -> handleExit());
    }

    // ✅ Allow pressing Enter to trigger login
    @FXML
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            login();
        }
    }

    // ✅ Login logic (case-insensitive username, case-sensitive password)
    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please enter both username and password.");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Use LOWER() for username and BINARY for password
            String sql = "SELECT `type` FROM `Login` WHERE LOWER(`username`) = LOWER(?) AND BINARY `password` = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String type = rs.getString("type");

                if (type.equalsIgnoreCase("admin")) {
                    loadScreen("/admin_dashboard.fxml", "Admin Dashboard");
                } else {
                    loadScreen("/member_dashboard.fxml", "Member Dashboard");
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.\n" + ex.getMessage());
        }
    }

    // ✅ Helper: Load next screen
    private void loadScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginBtn.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to load screen: " + fxmlPath);
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register_screen.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) registerBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open registration screen.");
        }
    }

    // ✅ Exit button handler
    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit FitLife");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Your progress will not be saved.");

        ButtonType yesButton = new ButtonType("Yes, Exit", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        alert.showAndWait().ifPresent(result -> {
            if (result == yesButton) {
                System.exit(0);
            }
        });
    }

    // ✅ Common alert helper
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
