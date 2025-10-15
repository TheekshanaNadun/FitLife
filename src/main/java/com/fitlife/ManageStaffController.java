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
    private AdminDashboardController dashboardController;

    // Setter called from AdminDashboardController
    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    public void initialize() {
        // ✅ Bind table columns to Staff model properties
        colId.setCellValueFactory(data -> data.getValue().idProperty());
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colGender.setCellValueFactory(data -> data.getValue().genderProperty());
        colRole.setCellValueFactory(data -> data.getValue().roleProperty());
        colContact.setCellValueFactory(data -> data.getValue().contactProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colSalary.setCellValueFactory(data -> data.getValue().salaryProperty().asString());

        // ✅ Make table columns resize evenly
        staffTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ✅ Load staff data from the database
        loadStaffFromDatabase();

        // ✅ Initialize dropdowns
        roleField.setItems(FXCollections.observableArrayList("Trainer", "Reception", "Cleaner", "Nutritionist"));
        genderField.setItems(FXCollections.observableArrayList("Male", "Female"));

        // ✅ Handle salary autofill based on role
        roleField.setOnAction(e -> setSalary());

        // ✅ Fill input fields when a row is selected
        staffTable.setOnMouseClicked(e -> fillFormFromSelection());

        // ✅ Back & Exit button actions
        if (backButton != null) backButton.setOnAction(e -> goBack());
        if (exitButton != null) exitButton.setOnAction(e -> exitApp());
    }


    // 🔹 Fetch all staff from DB
    private void loadStaffFromDatabase() {
        staffList.clear();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Staff")) {

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

            staffTable.setItems(null);
            staffTable.layout();
            staffTable.setItems(staffList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Auto set salary by role
    private void setSalary() {
        String role = roleField.getValue();
        if (role == null || role.isEmpty()) {
            salaryField.clear();
            return;
        }

        switch (role) {
            case "Trainer" -> salaryField.setText("70000");
            case "Reception" -> salaryField.setText("40000");
            case "Cleaner" -> salaryField.setText("30000");
            case "Nutritionist" -> salaryField.setText("60000");
            default -> salaryField.clear();
        }
    }

    // 🔹 Add new staff
    @FXML
    private void addStaff() {
        // --- Validate Inputs ---
        if (!validateInputs()) {
            return; // Stop if any field fails validation
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            // Check if contact or email already exists
            PreparedStatement check = conn.prepareStatement("SELECT * FROM Staff WHERE email=? OR contact=?");
            check.setString(1, emailField.getText());
            check.setString(2, contactField.getText());
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                showAlert(Alert.AlertType.WARNING, "Duplicate Entry",
                        "A staff member with this email or contact number already exists.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Staff (name, gender, role, contact, email, salary) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, nameField.getText().trim());
            ps.setString(2, genderField.getValue());
            ps.setString(3, roleField.getValue());
            ps.setString(4, contactField.getText().trim());
            ps.setString(5, emailField.getText().trim());
            ps.setInt(6, Integer.parseInt(salaryField.getText().trim()));

            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Staff added successfully!");
            loadStaffFromDatabase();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add staff. Please check your input.");
        }
    }

    // 🔹 Update selected staff
    @FXML
    private void updateStaff() {
        Staff selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a staff member to update.");
            return;
        }

        if (!validateInputs()) {
            return; // Stop if any field fails validation
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Staff SET name=?, gender=?, role=?, contact=?, email=?, salary=? WHERE id=?"
            );
            ps.setString(1, nameField.getText().trim());
            ps.setString(2, genderField.getValue());
            ps.setString(3, roleField.getValue());
            ps.setString(4, contactField.getText().trim());
            ps.setString(5, emailField.getText().trim());
            ps.setInt(6, Integer.parseInt(salaryField.getText().trim()));
            ps.setString(7, selected.getId());

            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Staff record updated successfully!");
            loadStaffFromDatabase();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update staff record.");
        }
    }
    // 🔹 Validate input fields with regex + field highlighting
    private boolean validateInputs() {
        // Reset borders
        nameField.setStyle(null);
        contactField.setStyle(null);
        emailField.setStyle(null);
        salaryField.setStyle(null);
        genderField.setStyle(null);
        roleField.setStyle(null);

        // Regex patterns
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.\\w+$";
        String contactRegex = "\\d{10}";
        String salaryRegex = "\\d+";

        if (nameField.getText().trim().isEmpty()) {
            highlightError(nameField, "Name cannot be empty.");
            return false;
        }
        if (genderField.getValue() == null) {
            highlightError(genderField, "Please select a gender.");
            return false;
        }
        if (roleField.getValue() == null) {
            highlightError(roleField, "Please select a role.");
            return false;
        }
        if (!contactField.getText().matches(contactRegex)) {
            highlightError(contactField, "Enter a valid 10-digit contact number (e.g., 0771234567).");
            return false;
        }
        if (!emailField.getText().matches(emailRegex)) {
            highlightError(emailField, "Enter a valid email address (e.g., user@mail.com).");
            return false;
        }
        if (!salaryField.getText().matches(salaryRegex)) {
            highlightError(salaryField, "Salary must be a number.");
            return false;
        }

        return true;
    }

    // 🔹 Helper to show red border + alert
    private void highlightError(Control field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 1.5; -fx-background-color: #ffeeee;");
        showAlert(Alert.AlertType.WARNING, "Validation Error", message);
    }

    // 🔹 Delete selected staff
    @FXML
    private void deleteStaff() {
        Staff selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a staff member to delete.");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Staff WHERE id=?");
            ps.setString(1, selected.getId());
            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Staff record deleted successfully!");
            loadStaffFromDatabase();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete staff record.");
        }
    }

    // 🔹 Fill form when a table row is clicked
    private void fillFormFromSelection() {
        Staff selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        nameField.setText(selected.getName());
        genderField.setValue(selected.getGender());
        roleField.setValue(selected.getRole());
        contactField.setText(selected.getContact());
        emailField.setText(selected.getEmail());
        salaryField.setText(String.valueOf(selected.getSalary()));
    }

    // 🔹 Clear all form fields
    @FXML
    private void clearAllStaff() {
        clearFields();
    }

    private void clearFields() {
        nameField.clear();
        contactField.clear();
        emailField.clear();
        salaryField.clear();

        roleField.setOnAction(null);
        genderField.getSelectionModel().clearSelection();
        roleField.getSelectionModel().clearSelection();
        roleField.setOnAction(e -> setSalary());
    }

    // 🔹 Return to dashboard
    @FXML
    private void goBack() {
        if (dashboardController != null) {
            dashboardController.showHome();
        } else {
            System.out.println("Dashboard controller is null!");
        }
    }

    // 🔹 Exit app
    @FXML
    private void exitApp() {
        Platform.exit();
    }

    // 🔹 Alert utility
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

