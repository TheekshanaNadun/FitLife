package com.fitlife;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.fitlife.model.Program;
import java.sql.*;

public class ManageProgramsController {

    // 🔹 FXML UI Elements
    @FXML private TextField programNameField;
    @FXML private TextField costField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> trainerField; // ✅ Changed from TextField → ComboBox<String>
    @FXML private TextField searchField;

    @FXML private TableView<Program> programTable;
    @FXML private TableColumn<Program, String> colProgramId;
    @FXML private TableColumn<Program, String> colProgramName;
    @FXML private TableColumn<Program, Number> colCostPerSession;
    @FXML private TableColumn<Program, String> colDescription;
    @FXML private TableColumn<Program, String> colTrainer;

    private ObservableList<Program> programList = FXCollections.observableArrayList();
    private ObservableList<String> trainerList = FXCollections.observableArrayList();
    private Connection conn;
    private AdminDashboardController dashboardController;

    // 🔹 Initialize
    @FXML
    public void initialize() {
        try {
            conn = DatabaseUtil.getConnection();
            bindTableColumns();
            loadPrograms();
            loadTrainers(); // ✅ Load trainers into ComboBox
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to the database.");
        }

        // Table selection listener
        programTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                programNameField.setText(newSel.getName());
                costField.setText(String.valueOf(newSel.getCostPerSession()));
                descriptionField.setText(newSel.getDescription());
                trainerField.setValue(newSel.getTrainer());
            }
        });
    }

    // 🔹 Load Trainer Names (Role = 'Trainer')
    private void loadTrainers() {
        trainerList.clear();
        String query = "SELECT name FROM Staff WHERE role = 'Trainer'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                trainerList.add(rs.getString("name"));
            }
            trainerField.setItems(trainerList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load trainers list.");
        }
    }

    // 🔹 Link columns to Program model
    private void bindTableColumns() {
        colProgramId.setCellValueFactory(data -> data.getValue().idProperty());
        colProgramName.setCellValueFactory(data -> data.getValue().nameProperty());
        colCostPerSession.setCellValueFactory(data -> data.getValue().costPerSessionProperty());
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        colTrainer.setCellValueFactory(data -> data.getValue().trainerProperty());
        programTable.setItems(programList);
    }

    // 🔹 Load all programs
    private void loadPrograms() {
        programList.clear();
        String query = "SELECT * FROM Program";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
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

    // 🔹 Add new program
    @FXML
    private void addProgram() {
        if (!validateInputs()) return;

        String query = "INSERT INTO Program (name, cost_per_session, description, trainer) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, programNameField.getText().trim());
            ps.setInt(2, Integer.parseInt(costField.getText().trim()));
            ps.setString(3, descriptionField.getText().trim());
            ps.setString(4, trainerField.getValue());
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Program added successfully!");
            loadPrograms();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add program.");
        }
    }

    // 🔹 Update existing program
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
            ps.setString(4, trainerField.getValue());
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

    // 🔹 Delete program
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

    // 🔹 Search programs
    @FXML
    private void searchPrograms() {
        String query = searchField.getText().trim().toLowerCase();
        ObservableList<Program> filteredList = FXCollections.observableArrayList();

        for (Program p : programList) {
            if (p.getName().toLowerCase().contains(query)
                    || p.getTrainer().toLowerCase().contains(query)
                    || p.getDescription().toLowerCase().contains(query)) {
                filteredList.add(p);
            }
        }

        programTable.setItems(filteredList);
        if (query.isEmpty()) programTable.setItems(programList);
    }

    // 🔹 Clear fields
    @FXML
    private void clearAllPrograms() {
        clearFields();
    }

    private void clearFields() {
        programNameField.clear();
        costField.clear();
        descriptionField.clear();
        trainerField.getSelectionModel().clearSelection();
        programTable.getSelectionModel().clearSelection();
    }

    // 🔹 Input validation
    private boolean validateInputs() {
        String name = programNameField.getText().trim();
        String cost = costField.getText().trim();
        String desc = descriptionField.getText().trim();
        String trainer = trainerField.getValue();

        if (name.isEmpty()) return showValidationError("Program name is required.");
        if (!cost.matches("\\d+")) return showValidationError("Cost must be a valid number.");
        if (desc.isEmpty()) return showValidationError("Description cannot be empty.");
        if (trainer == null || trainer.isEmpty()) return showValidationError("Please select a trainer.");

        return true;
    }

    private boolean showValidationError(String msg) {
        showAlert(Alert.AlertType.WARNING, "Validation Error", msg);
        return false;
    }

    // 🔹 Alerts
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        if (dashboardController != null) dashboardController.showHome();
    }

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }
}
