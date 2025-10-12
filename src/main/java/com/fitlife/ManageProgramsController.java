package com.fitlife;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import com.fitlife.model.Program;
import java.sql.*;

public class ManageProgramsController {

    @FXML private TextField programNameField, durationField, priceField;
    @FXML private TableView<Program> programTable;
    @FXML private TableColumn<Program, Integer> colProgramId;
    @FXML private TableColumn<Program, String> colProgramName;
    @FXML private TableColumn<Program, Integer> colDuration;
    @FXML private TableColumn<Program, Double> colPrice;
    @FXML private Button backButton, exitButton;

    private ObservableList<Program> programList = FXCollections.observableArrayList();
    private Connection conn;

    // Reference to AdminDashboardController
    private AdminDashboardController dashboardController;

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void initialize() {
        try {
            conn = DatabaseUtil.getConnection();
            bindTableColumns();
            loadPrograms();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database connection failed!");
        }

        programTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                programNameField.setText(newSel.getName());
                durationField.setText(String.valueOf(newSel.getDuration()));
                priceField.setText(String.valueOf(newSel.getPrice()));
            }
        });

        if (backButton != null) backButton.setOnAction(e -> goBack());
        if (exitButton != null) exitButton.setOnAction(e -> exitApp());
    }

    private void bindTableColumns() {
        colProgramId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colProgramName.setCellValueFactory(data -> data.getValue().nameProperty());
        colDuration.setCellValueFactory(data -> data.getValue().durationProperty().asObject());
        colPrice.setCellValueFactory(data -> data.getValue().priceProperty().asObject());
        programTable.setItems(programList);
    }

    private void loadPrograms() {
        programList.clear();
        String query = "SELECT * FROM Program"; // ✅ make sure table name is lowercase if DB uses that
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                programList.add(new Program(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("duration"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to load programs!");
        }
    }

    @FXML
    private void addProgram() {
        try {
            String name = programNameField.getText();
            int duration = Integer.parseInt(durationField.getText());
            double price = Double.parseDouble(priceField.getText());

            String query = "INSERT INTO Program (name, duration, price) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, name);
                ps.setInt(2, duration);
                ps.setDouble(3, price);
                ps.executeUpdate();
            }
            loadPrograms();
            clearAllPrograms();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to add program!");
        }
    }

    @FXML
    private void updateProgram() {
        Program selected = programTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            String name = programNameField.getText();
            int duration = Integer.parseInt(durationField.getText());
            double price = Double.parseDouble(priceField.getText());

            String query = "UPDATE Program SET name=?, duration=?, price=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, name);
                ps.setInt(2, duration);
                ps.setDouble(3, price);
                ps.setInt(4, selected.getId());
                ps.executeUpdate();
            }
            loadPrograms();
            clearAllPrograms();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to update program!");
        }
    }

    @FXML
    private void deleteProgram() {
        Program selected = programTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            String query = "DELETE FROM Program WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, selected.getId());
                ps.executeUpdate();
            }
            loadPrograms();
            clearAllPrograms();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to delete program!");
        }
    }

    @FXML
    private void clearAllPrograms() {
        programNameField.clear();
        durationField.clear();
        priceField.clear();
        programTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ Fixed Back — no duplicate dashboard
    @FXML
    private void goBack() {
        if (dashboardController != null) {
            dashboardController.showHome();
        } else {
            System.out.println("Dashboard controller is null!");
        }
    }

    @FXML
    private void exitApp() {
        Platform.exit();
    }
}
