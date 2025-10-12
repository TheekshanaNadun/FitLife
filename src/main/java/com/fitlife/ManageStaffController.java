package com.fitlife;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import com.fitlife.model.Staff;

public class ManageStaffController {

    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, String> colId, colName, colGender, colRole, colContact, colEmail, colSalary;

    @FXML private TextField nameField, contactField, emailField, salaryField;
    @FXML private ComboBox<String> genderField, roleField;

    @FXML private Button backButton, exitButton;

    private ObservableList<Staff> staffList = FXCollections.observableArrayList();
    private AdminDashboardController dashboardController; // ✅ Reference to dashboard

    // Setter called from AdminDashboardController
    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void initialize() {
        loadStaffFromDatabase();
        roleField.setOnAction(e -> setSalary());

        if (backButton != null) backButton.setOnAction(e -> goBack());
        if (exitButton != null) exitButton.setOnAction(e -> exitApp());
    }

    private void loadStaffFromDatabase() {
        staffList.clear();
        try (Connection conn = DatabaseUtil.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Staff");
            while (rs.next()) {
                staffList.add(new Staff(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getString("role"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getInt("salary")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        staffTable.setItems(staffList);
    }

    private void setSalary() {
        String role = roleField.getValue();
        switch (role) {
            case "Trainer" -> salaryField.setText("70000");
            case "Reception" -> salaryField.setText("40000");
            case "Cleaner" -> salaryField.setText("30000");
            case "Nutritionist" -> salaryField.setText("60000");
        }
    }

    @FXML
    private void addStaff() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Staff (name, gender, role, contact, email, salary) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, nameField.getText());
            ps.setString(2, genderField.getValue());
            ps.setString(3, roleField.getValue());
            ps.setString(4, contactField.getText());
            ps.setString(5, emailField.getText());
            ps.setInt(6, Integer.parseInt(salaryField.getText()));
            ps.executeUpdate();
            loadStaffFromDatabase();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateStaff() {
        // TODO: Implement UPDATE query logic
    }

    @FXML
    private void deleteStaff() {
        // TODO: Implement DELETE query logic
    }

    @FXML
    private void clearAllStaff() { clearFields(); }

    private void clearFields() {
        nameField.clear();
        contactField.clear();
        emailField.clear();
        salaryField.clear();
        genderField.getSelectionModel().clearSelection();
        roleField.getSelectionModel().clearSelection();
    }

    // ✅ Fixed Back — returns to same dashboard (no duplicate)
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
