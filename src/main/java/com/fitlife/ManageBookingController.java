package com.fitlife;

import com.fitlife.model.Booking;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class ManageBookingController {

    // --- FXML Components ---
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private ComboBox<String> membershipField;
    @FXML private ComboBox<String> programField;
    @FXML private DatePicker startDateField;
    @FXML private TextField sessionsField;
    @FXML private TextField totalCostField;

    // --- TableView ---
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> colBookingId;
    @FXML private TableColumn<Booking, String> colName;
    @FXML private TableColumn<Booking, String> colContact;
    @FXML private TableColumn<Booking, String> colMembership;
    @FXML private TableColumn<Booking, String> colProgram;
    @FXML private TableColumn<Booking, String> colStartDate;
    @FXML private TableColumn<Booking, String> colSessions;
    @FXML private TableColumn<Booking, String> colTotalCost;

    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();

    // --- Initialization ---
    @FXML
    public void initialize() {
        membershipField.setItems(FXCollections.observableArrayList("Monthly", "Quarterly", "Annual"));
        setupTable();
        loadProgramsFromDatabase();
        loadBookingsFromDatabase();
        bookingTable.setOnMouseClicked(this::handleTableClick);
    }

    private void setupTable() {
        colBookingId.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("B%03d", cell.getValue().getId())));
        colName.setCellValueFactory(cell -> cell.getValue().fullNameProperty());
        colContact.setCellValueFactory(cell -> cell.getValue().contactProperty());
        colMembership.setCellValueFactory(cell -> cell.getValue().membershipProperty());
        colProgram.setCellValueFactory(cell -> cell.getValue().programProperty());
        colStartDate.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStartDate() == null ? "" :
                        cell.getValue().getStartDate().toString()));
        colSessions.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getNumSessions())));
        colTotalCost.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getTotalCost())));

        bookingTable.setItems(bookingList);
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // --- Calculate Total Cost ---
    @FXML
    private void calculateTotalCost() {
        String s = sessionsField.getText().trim();
        if (s.isEmpty()) {
            totalCostField.clear();
            return;
        }
        try {
            int sessions = Integer.parseInt(s);
            if (sessions <= 0) throw new NumberFormatException();

            int rate = (sessions <= 10) ? 1000 :
                    (sessions <= 30) ? 800 :
                            (sessions <= 50) ? 600 : 500;

            totalCostField.setText(String.valueOf(sessions * rate));
        } catch (NumberFormatException ex) {
            totalCostField.setText("Invalid");
        }
    }

    // --- Create Booking ---
    @FXML
    private void createBooking() {
        if (!validateBookingInputs()) return;

        try (Connection conn = DatabaseUtil.getConnection()) {
            String insertSql = "INSERT INTO Bookings (full_name, contact, membership_type, program, start_date, sessions, total_cost) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, nameField.getText().trim());
            ps.setString(2, contactField.getText().trim());
            ps.setString(3, membershipField.getValue());
            ps.setString(4, programField.getValue());
            ps.setDate(5, startDateField.getValue() == null ? null : Date.valueOf(startDateField.getValue()));
            ps.setInt(6, Integer.parseInt(sessionsField.getText().trim()));
            ps.setInt(7, Integer.parseInt(totalCostField.getText().trim()));
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                Booking b = new Booking(
                        id,
                        nameField.getText().trim(),
                        contactField.getText().trim(),
                        membershipField.getValue(),
                        programField.getValue(),
                        startDateField.getValue(),
                        Integer.parseInt(sessionsField.getText().trim()),
                        Integer.parseInt(totalCostField.getText().trim())
                );
                bookingList.add(b);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking created: " + formatBookingId(id));
                clearFields();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
        }
    }

    private String formatBookingId(int id) {
        return String.format("B%03d", id);
    }

    // --- Edit Booking ---
    @FXML
    private void editBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection", "Select a booking to edit.");
            return;
        }
        if (!validateBookingInputs()) return;

        selected.setFullName(nameField.getText().trim());
        selected.setContact(contactField.getText().trim());
        selected.setMembership(membershipField.getValue());
        selected.setProgram(programField.getValue());
        selected.setStartDate(startDateField.getValue());
        selected.setNumSessions(Integer.parseInt(sessionsField.getText().trim()));
        selected.setTotalCost(Integer.parseInt(totalCostField.getText().trim()));

        String updateSql = "UPDATE Bookings SET full_name=?, contact=?, membership_type=?, program=?, start_date=?, sessions=?, total_cost=? WHERE booking_id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, selected.getFullName());
            ps.setString(2, selected.getContact());
            ps.setString(3, selected.getMembership());
            ps.setString(4, selected.getProgram());
            ps.setDate(5, selected.getStartDate() == null ? null : Date.valueOf(selected.getStartDate()));
            ps.setInt(6, selected.getNumSessions());
            ps.setInt(7, selected.getTotalCost());
            ps.setInt(8, selected.getId());

            ps.executeUpdate();
            bookingTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Booking updated successfully.");
            clearFields();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
        }
    }

    // --- Delete Booking ---
    @FXML
    private void deleteBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection", "Select a booking to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete booking " + formatBookingId(selected.getId()) + "?",
                ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.YES) return;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Bookings WHERE booking_id=?")) {
            ps.setInt(1, selected.getId());
            ps.executeUpdate();
            bookingList.remove(selected);
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Booking deleted successfully.");
            clearFields();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- Load Data ---
    private void loadProgramsFromDatabase() {
        programField.getItems().clear();
        String sql = "SELECT Name FROM Program";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                programField.getItems().add(rs.getString("Name"));
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
        }
    }

    private void loadBookingsFromDatabase() {
        bookingList.clear();
        String sql = "SELECT * FROM Bookings ORDER BY booking_id";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Booking b = new Booking(
                        rs.getInt("booking_id"),
                        rs.getString("full_name"),
                        rs.getString("contact"),
                        rs.getString("membership_type"),
                        rs.getString("program"),
                        rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate(),
                        rs.getInt("sessions"),
                        rs.getInt("total_cost")
                );
                bookingList.add(b);
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
        }
    }

    // --- Helpers ---
    private void handleTableClick(MouseEvent e) {
        if (e.getClickCount() == 1) fillFormFromSelection();
    }

    private void fillFormFromSelection() {
        Booking s = bookingTable.getSelectionModel().getSelectedItem();
        if (s == null) return;
        nameField.setText(s.getFullName());
        contactField.setText(s.getContact());
        membershipField.setValue(s.getMembership());
        programField.setValue(s.getProgram());
        startDateField.setValue(s.getStartDate());
        sessionsField.setText(String.valueOf(s.getNumSessions()));
        totalCostField.setText(String.valueOf(s.getTotalCost()));
    }

    @FXML
    public void clearFields() {
        nameField.clear();
        contactField.clear();
        membershipField.getSelectionModel().clearSelection();
        programField.getSelectionModel().clearSelection();
        startDateField.setValue(null);
        sessionsField.clear();
        totalCostField.clear();
        bookingTable.getSelectionModel().clearSelection();
    }

    private boolean validateBookingInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Full name is required.");
            return false;
        }
        if (!contactField.getText().matches("\\d{10}")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Contact must be 10 digits.");
            return false;
        }
        if (membershipField.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Select membership type.");
            return false;
        }
        if (programField.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Select program.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/member_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) bookingTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Member Main Menu");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to go back to main menu.");
        }
    }

    @FXML
    private void exitApp() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to exit?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) Platform.exit();
        });
    }
}
