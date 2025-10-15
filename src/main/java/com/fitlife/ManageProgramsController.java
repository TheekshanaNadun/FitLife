package com.fitlife;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.fitlife.model.Program;
import java.sql.*;

public class ManageProgramsController {

    @FXML private TextField programNameField, costField, descriptionField, trainerField;
    @FXML private TableView<Program> programTable;
    @FXML private TableColumn<Program, String> colProgramId;  // ✅ id is StringProperty
    @FXML private TableColumn<Program, String> colProgramName;
    @FXML private TableColumn<Program, Number> colCostPerSession; // ✅ cost is IntegerProperty
    @FXML private TableColumn<Program, String> colDescription;
    @FXML private TableColumn<Program, String> colTrainer;
    @FXML private Button backButton, exitButton;

    private ObservableList<Program> programList = FXCollections.observableArrayList();
    private Connection conn;
    private AdminDashboardController dashboardController;

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    // 🔹 Initialize
    @FXML
    public void initialize() {
        try {
            conn = DatabaseUtil.getConnection();
            bindTableColumns();
            loadPrograms();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database.");
        }

        // Table click → fill form
        programTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                programNameField.setText(newSel.getName());
                costField.setText(String.valueOf(newSel.getCostPerSession()));
                descriptionField.setText(newSel.getDescription());
                trainerField.setText(newSel.getTrainer());
            }
        });

        if (backButton != null) backButton.setOnAction(e -> goBack());
        if (exitButton != null) exitButton.setOnAction(e -> exitApp());
    }

    // 🔹 Bind columns to Program model
    private void bindTableColumns() {
        colProgramId.setCellValueFactory(data -> data.getValue().idProperty());
        colProgramName.setCellValueFactory(data -> data.getValue().nameProperty());
        colCostPerSession.setCellValueFactory(data -> data.getValue().costPerSessionProperty());
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        colTrainer.setCellValueFactory(data -> data.getValue().trainerProperty());
        programTable.setItems(programList);
    }

    // 🔹 Load all programs from DB
    private void loadPrograms() {
        programList.clear();
        String query = "SELECT * FROM Program";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                programList.add(new Program(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("cost_per_session"),
                        rs.getString("description"),
                        rs.getString("trainer")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load program data.");
        }
    }

    // 🔹 Add Program
    @FXML
    private void addProgram() {
        if (!validateInputs()) return;

        String query = "INSERT INTO Program (name, cost_per_session, description, trainer) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, programNameField.getText().trim());
            ps.setInt(2, Integer.parseInt(costField.getText().trim()));
            ps.setString(3, descriptionField.getText().trim());
            ps.setString(4, trainerField.getText().trim());
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Program added successfully!");
            loadPrograms();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add program.");
        }
    }

    // 🔹 Update Program
    @FXML
    private void updateProgram() {
        Program selected = programTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a program to update.");
            return;
        }
        if (!validateInputs()) return;

        String query = "UPDATE Program SET name=?, cost_per_session=?, description=?, trainer=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, programNameField.getText().trim());
            ps.setInt(2, Integer.parseInt(costField.getText().trim()));
            ps.setString(3, descriptionField.getText().trim());
            ps.setString(4, trainerField.getText().trim());
            ps.setString(5, selected.getId());
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Program updated successfully!");
            loadPrograms();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update program.");
        }
    }

    // 🔹 Delete Program
    @FXML
    private void deleteProgram() {
        Program selected = programTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a program to delete.");
            return;
        }

        String query = "DELETE FROM Program WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, selected.getId());
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Program deleted successfully!");
            loadPrograms();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete program.");
        }
    }

    // 🔹 Clear Fields
    @FXML
    private void clearAllPrograms() {
        clearFields();
    }

    private void clearFields() {
        programNameField.clear();
        costField.clear();
        descriptionField.clear();
        trainerField.clear();
        programTable.getSelectionModel().clearSelection();
        resetFieldStyles();
    }

    // 🔹 Validation
    private boolean validateInputs() {
        resetFieldStyles();

        String name = programNameField.getText().trim();
        String cost = costField.getText().trim();
        String desc = descriptionField.getText().trim();
        String trainer = trainerField.getText().trim();

        if (name.isEmpty()) return highlightError(programNameField, "Program name is required.");
        if (!cost.matches("\\d+")) return highlightError(costField, "Cost must be a valid number.");
        if (desc.isEmpty()) return highlightError(descriptionField, "Description cannot be empty.");
        if (trainer.isEmpty() || !trainer.matches("^[A-Za-z ]+$"))
            return highlightError(trainerField, "Trainer name must contain only letters.");

        return true;
    }

    private void resetFieldStyles() {
        programNameField.setStyle(null);
        costField.setStyle(null);
        descriptionField.setStyle(null);
        trainerField.setStyle(null);
    }

    private boolean highlightError(Control field, String message) {
        field.setStyle("-fx-border-color: red; -fx-background-color: #ffeeee;");
        showAlert(Alert.AlertType.WARNING, "Validation Error", message);
        return false;
    }

    // 🔹 Alert Utility
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 🔹 Navigation
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
