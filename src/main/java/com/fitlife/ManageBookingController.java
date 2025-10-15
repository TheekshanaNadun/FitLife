package com.fitlife;

import javafx.application.Platform;
import javafx.beans.property.*;
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

    // Form fields (from your FXML)
    @FXML private TextField bookingIdField;
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private ComboBox<String> membershipField;
    @FXML private ComboBox<String> programField;
    @FXML private DatePicker startDateField;
    @FXML private TextField sessionsField;
    @FXML private TextField totalCostField;

    // Table and columns
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> colBookingId, colName, colContact, colMembership, colProgram, colStartDate, colSessions, colTotalCost;

    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize membership types and program list placeholder (programs will be loaded from DB)
        membershipField.setItems(FXCollections.observableArrayList("Monthly", "Quarterly", "Annual"));

        // Configure table columns to read from Booking properties
        colBookingId.setCellValueFactory(cell -> cell.getValue().bookingIdProperty());
        colName.setCellValueFactory(cell -> cell.getValue().fullNameProperty());
        colContact.setCellValueFactory(cell -> cell.getValue().contactProperty());
        colMembership.setCellValueFactory(cell -> cell.getValue().membershipProperty());
        colProgram.setCellValueFactory(cell -> cell.getValue().programProperty());
        colStartDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStartDate() == null ? "" : cell.getValue().getStartDate().toString()));
        colSessions.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getNumSessions())));
        colTotalCost.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getTotalCost())));

        bookingTable.setItems(bookingList);
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // load programs into programField and load existing bookings
        loadProgramsFromDatabase();
        loadBookingsFromDatabase();

        // clicking a row fills the form
        bookingTable.setOnMouseClicked(this::handleTableClick);
    }

    // Called onKeyReleased for sessionsField in your FXML
    @FXML
    private void calculateTotalCost() {
        String s = sessionsField.getText().trim();
        if (s.isEmpty()) {
            totalCostField.clear();
            return;
        }
        int sessions;
        try {
            sessions = Integer.parseInt(s);
            if (sessions < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            totalCostField.setText("Invalid");
            return;
        }

        int rate;
        if (sessions <= 10) rate = 1000;
        else if (sessions <= 30) rate = 800;
        else if (sessions <= 50) rate = 600;
        else rate = 500;

        long total = (long) sessions * rate;
        totalCostField.setText(String.valueOf(total));
    }

    @FXML
    private void createBooking() {
        if (!validateBookingInputs()) return;

        String id = bookingIdField.getText().trim();
        if (id.isEmpty()) id = generateNextBookingId();

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

        // Insert into DB
        String insertSql = "INSERT INTO Bookings (booking_id, full_name, contact, membership_type, program, start_date, num_sessions, total_cost) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setString(1, b.getBookingId());
            ps.setString(2, b.getFullName());
            ps.setString(3, b.getContact());
            ps.setString(4, b.getMembership());
            ps.setString(5, b.getProgram());
            ps.setDate(6, b.getStartDate() == null ? null : Date.valueOf(b.getStartDate()));
            ps.setInt(7, b.getNumSessions());
            ps.setInt(8, b.getTotalCost());

            ps.executeUpdate();
            bookingList.add(b);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Booking created: " + b.getBookingId());
            clearFields();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create booking: " + ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

        String updateSql = "UPDATE Bookings SET full_name=?, contact=?, membership_type=?, program=?, start_date=?, num_sessions=?, total_cost=? WHERE booking_id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, selected.getFullName());
            ps.setString(2, selected.getContact());
            ps.setString(3, selected.getMembership());
            ps.setString(4, selected.getProgram());
            ps.setDate(5, selected.getStartDate() == null ? null : Date.valueOf(selected.getStartDate()));
            ps.setInt(6, selected.getNumSessions());
            ps.setInt(7, selected.getTotalCost());
            ps.setString(8, selected.getBookingId());

            ps.executeUpdate();
            bookingTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Booking updated.");
            clearFields();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update booking: " + ex.getMessage());
        }
    }

    @FXML
    private void deleteBooking() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No selection", "Select a booking to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete booking " + selected.getBookingId() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.YES) return;

        String deleteSql = "DELETE FROM Bookings WHERE booking_id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, selected.getBookingId());
            ps.executeUpdate();
            bookingList.remove(selected);
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Booking deleted.");
            clearFields();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete booking: " + ex.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        bookingIdField.setText(generateNextBookingId());
        nameField.clear();
        contactField.clear();
        membershipField.getSelectionModel().clearSelection();
        programField.getSelectionModel().clearSelection();
        startDateField.setValue(null);
        sessionsField.clear();
        totalCostField.clear();
        bookingTable.getSelectionModel().clearSelection();
    }

    // --- Helpers ---

    private void handleTableClick(MouseEvent e) {
        if (e.getClickCount() == 1) fillFormFromSelection();
    }

    private void fillFormFromSelection() {
        Booking s = bookingTable.getSelectionModel().getSelectedItem();
        if (s == null) return;
        bookingIdField.setText(s.getBookingId());
        nameField.setText(s.getFullName());
        contactField.setText(s.getContact());
        membershipField.setValue(s.getMembership());
        programField.setValue(s.getProgram());
        startDateField.setValue(s.getStartDate());
        sessionsField.setText(String.valueOf(s.getNumSessions()));
        totalCostField.setText(String.valueOf(s.getTotalCost()));
    }

    private boolean validateBookingInputs() {
        // Simple validation, expand as needed
        if (nameField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Full name is required.");
            return false;
        }
        if (!contactField.getText().matches("\\d{10}")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Contact must be 10 digits.");
            return false;
        }
        if (membershipField.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Select membership type.");
            return false;
        }
        if (programField.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Select a program.");
            return false;
        }
        try {
            int s = Integer.parseInt(sessionsField.getText().trim());
            if (s <= 0) { showAlert(Alert.AlertType.WARNING, "Validation", "Sessions must be positive."); return false; }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Enter valid number of sessions.");
            return false;
        }
        if (totalCostField.getText().trim().isEmpty() || totalCostField.getText().equals("Invalid")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Total cost invalid.");
            return false;
        }
        return true;
    }

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
            ex.printStackTrace();
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
                        rs.getString("booking_id"),
                        rs.getString("full_name"),
                        rs.getString("contact"),
                        rs.getString("membership_type"),
                        rs.getString("program"),
                        rs.getDate("start_date") == null ? null : rs.getDate("start_date").toLocalDate(),
                        rs.getInt("num_sessions"),
                        rs.getInt("total_cost")
                );
                bookingList.add(b);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String generateNextBookingId() {
        String next = "B001";
        String sql = "SELECT booking_id FROM Bookings ORDER BY booking_id DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("booking_id"); // e.g. B012
                if (last != null && last.length() > 1) {
                    String numPart = last.substring(1);
                    int n = Integer.parseInt(numPart);
                    n++;
                    next = String.format("B%03d", n);
                }
            }
        } catch (Exception ex) {
            // If DB unavailable or parse fail, fallback to B001
        }
        return next;
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

            Stage stage = (Stage) bookingIdField.getScene().getWindow();
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



    // --- Inner Booking model (JavaFX properties) ---
    public static class Booking {
        private final StringProperty bookingId = new SimpleStringProperty();
        private final StringProperty fullName = new SimpleStringProperty();
        private final StringProperty contact = new SimpleStringProperty();
        private final StringProperty membership = new SimpleStringProperty();
        private final StringProperty program = new SimpleStringProperty();
        private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
        private final IntegerProperty numSessions = new SimpleIntegerProperty();
        private final IntegerProperty totalCost = new SimpleIntegerProperty();

        public Booking(String bookingId, String fullName, String contact,
                       String membership, String program, LocalDate startDate,
                       int numSessions, int totalCost) {
            this.bookingId.set(bookingId);
            this.fullName.set(fullName);
            this.contact.set(contact);
            this.membership.set(membership);
            this.program.set(program);
            this.startDate.set(startDate);
            this.numSessions.set(numSessions);
            this.totalCost.set(totalCost);
        }

        // getters & setters and property accessors
        public String getBookingId() { return bookingId.get(); }
        public void setBookingId(String v) { bookingId.set(v); }
        public StringProperty bookingIdProperty() { return bookingId; }

        public String getFullName() { return fullName.get(); }
        public void setFullName(String v) { fullName.set(v); }
        public StringProperty fullNameProperty() { return fullName; }

        public String getContact() { return contact.get(); }
        public void setContact(String v) { contact.set(v); }
        public StringProperty contactProperty() { return contact; }

        public String getMembership() { return membership.get(); }
        public void setMembership(String v) { membership.set(v); }
        public StringProperty membershipProperty() { return membership; }

        public String getProgram() { return program.get(); }
        public void setProgram(String v) { program.set(v); }
        public StringProperty programProperty() { return program; }

        public LocalDate getStartDate() { return startDate.get(); }
        public void setStartDate(LocalDate d) { startDate.set(d); }
        public ObjectProperty<LocalDate> startDateProperty() { return startDate; }

        public int getNumSessions() { return numSessions.get(); }
        public void setNumSessions(int v) { numSessions.set(v); }
        public IntegerProperty numSessionsProperty() { return numSessions; }

        public int getTotalCost() { return totalCost.get(); }
        public void setTotalCost(int v) { totalCost.set(v); }
        public IntegerProperty totalCostProperty() { return totalCost; }
    }
}
