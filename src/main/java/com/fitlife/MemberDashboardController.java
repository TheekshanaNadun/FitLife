package com.fitlife;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MemberDashboardController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        showHome();
    }

    public void showHome() {
        loadCenterView("/MemberHome.fxml", controller -> {
            if (controller instanceof MemberHomeController homeController) {
                homeController.setDashboardController(this);
            }
        });
    }

    private void loadCenterView(String fxmlPath, java.util.function.Consumer<Object> controllerHandler) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane view = loader.load();

            Object controller = loader.getController();
            if (controllerHandler != null)
                controllerHandler.accept(controller);

            mainBorderPane.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading view: " + fxmlPath);
        }
    }

    @FXML
    private void logout() {
        try {
            // ✅ Adjust the path if your FXML is inside a subfolder
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainApp.fxml"));
            Pane loginRoot = loader.load();

            // ✅ Get current window (stage)
            javafx.stage.Stage stage = (javafx.stage.Stage) mainBorderPane.getScene().getWindow();

            // ✅ Replace the full scene
            javafx.scene.Scene scene = new javafx.scene.Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("FitLife Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error: Unable to load the login screen.\n" + e.getMessage());
        }
    }

    @FXML
    private void exitApp() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit FitLife");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Your progress will not be saved.");

        // Custom button colors
        ButtonType yesButton = new ButtonType("Yes, Exit", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        // Inline fallback styling (optional if CSS is missing)
        dialogPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #009688; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;"
        );

        alert.showAndWait().ifPresent(result -> {
            if (result == yesButton) {
                System.exit(0);
            }
        });
    }


    public BorderPane getMainBorderPane() {
        return mainBorderPane;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
