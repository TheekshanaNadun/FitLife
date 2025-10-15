package com.fitlife;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MemberHomeController {

    private MemberDashboardController dashboardController;

    public void setDashboardController(MemberDashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    private void openManageBookings() {
        loadPage("ManageBookings.fxml");
    }

    @FXML
    private void openViewPrograms() {
        loadPage("ViewPrograms.fxml");
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Pane view = loader.load();

            BorderPane mainPane = dashboardController.getMainBorderPane();
            mainPane.setCenter(view);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error loading " + fxml);
        }
    }
}
