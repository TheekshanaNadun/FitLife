package com.fitlife;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DashboardHomeController {

    private AdminDashboardController dashboardController;

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    private void openManageStaff(ActionEvent event) {
        dashboardController.openManageStaff();
    }

    @FXML
    private void openManagePrograms(ActionEvent event) {
        dashboardController.openManagePrograms();
    }

    @FXML private StackPane staffCard;
    @FXML private StackPane programCard;
}
