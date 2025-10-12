package com.fitlife;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class MemberDashboardController {

    @FXML
    private void openManageBookings(ActionEvent event) {
        loadScreen(event, "/manage_bookings.fxml", "Manage Bookings");
    }

    @FXML
    private void openViewPrograms(ActionEvent event) {
        loadScreen(event, "/view_programs.fxml", "View Programs");
    }

    private void loadScreen(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Replace current stage scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load screen: " + title);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
