package com.fitlife;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private BorderPane mainBorderPane;

    // ✅ Keep only one instance of the home dashboard
    private Parent homeContent;

    @FXML
    public void initialize() {
        loadHomeContent();
    }

    // ✅ Load dashboard home once
    private void loadHomeContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_home.fxml"));
            homeContent = loader.load();

            // Give home controller access to this one
            DashboardHomeController homeController = loader.getController();
            homeController.setDashboardController(this);

            mainBorderPane.setCenter(homeContent);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard home.");
        }
    }

    // ✅ Called by DashboardHomeController when user clicks “Staff”
    public void openManageStaff() {
        loadScreen("/manage_staff.fxml", "staff");
    }

    // ✅ Called by DashboardHomeController when user clicks “Programs”
    public void openManagePrograms() {
        loadScreen("/manage_program.fxml", "program");
    }

    // ✅ Reuse same home content (fix for duplicates)
    public void showHome() {
        mainBorderPane.setCenter(homeContent);
    }

    // ✅ Load sub-screens with back navigation support
    private void loadScreen(String fxmlPath, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            Object controller = loader.getController();

            // Let the controller know how to go back
            if (controller instanceof ManageStaffController staffController) {
                staffController.setDashboardController(this);
            } else if (controller instanceof ManageProgramsController programController) {
                programController.setDashboardController(this);
            }

            mainBorderPane.setCenter(content);
            System.out.println("Loaded " + type + " screen successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load screen: " + fxmlPath);
        }
    }

    // ✅ Reusable alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void exitApp() {
        Platform.exit();
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainApp.fxml"));
            Parent loginRoot = loader.load();
            mainBorderPane.setCenter(loginRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to log out.");
        }
    }
}
