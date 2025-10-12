package com.fitlife;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class DashboardHomeController {

    @FXML
    private VBox staffCard;

    @FXML
    private VBox programCard;

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
}
