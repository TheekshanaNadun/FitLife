package com.fitlife;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.sql.*;
import com.fitlife.model.Program;

public class ViewProgramsController {

    @FXML private TextField searchField;
    @FXML private TableView<Program> programTable;
    @FXML private TableColumn<Program, String> colId;
    @FXML private TableColumn<Program, String> colName;
    @FXML private TableColumn<Program, String> colDescription;
    @FXML private TableColumn<Program, String> colTrainer;
    @FXML private TableColumn<Program, Integer> colCost;

    private ObservableList<Program> programList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configure table columns to match Program model
        programTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        colCost.setCellValueFactory(cell -> cell.getValue().costPerSessionProperty().asObject());
        colDescription.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        colTrainer.setCellValueFactory(cell -> cell.getValue().trainerProperty());

        // Load all programs initially
        loadPrograms();
    }

    private void loadPrograms() {
        programList.clear();
        String sql = "SELECT * FROM Program";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                programList.add(new Program(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("cost_per_session"),
                        rs.getString("description"),
                        rs.getString("trainer")
                ));
            }
            programTable.setItems(programList);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to load program data.");
        }
    }

    @FXML
    private void searchPrograms() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            programTable.setItems(programList);
        } else {
            programTable.setItems(programList.filtered(p ->
                    p.getName().toLowerCase().contains(keyword) ||
                            p.getDescription().toLowerCase().contains(keyword) ||
                            p.getTrainer().toLowerCase().contains(keyword)
            ));
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/member_dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Member Main Menu");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to go back to main menu.");
        }
    }

    @FXML
    private void exitApp() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Platform.exit();
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
