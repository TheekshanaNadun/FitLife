package com.fitlife;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            mainBorderPane.getScene().setRoot(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error returning to login!");
        }
    }

    @FXML
    private void exitApp() {
        Platform.exit();
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
