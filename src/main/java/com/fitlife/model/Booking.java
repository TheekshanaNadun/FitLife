package com.fitlife.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Booking model class for FitLife Gym Management System.
 * Stores the real numeric ID (auto-incremented in DB)
 * and provides a formatted booking code (e.g., "B001") for UI display.
 */
public class Booking {

    private final IntegerProperty id;           // actual DB auto-increment value
    private final StringProperty fullName;
    private final StringProperty contact;
    private final StringProperty membership;
    private final StringProperty program;
    private final ObjectProperty<LocalDate> startDate;
    private final IntegerProperty numSessions;
    private final IntegerProperty totalCost;

    // ✅ Constructor
    public Booking(int id, String fullName, String contact,
                   String membership, String program, LocalDate startDate,
                   int numSessions, int totalCost) {

        this.id = new SimpleIntegerProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.contact = new SimpleStringProperty(contact);
        this.membership = new SimpleStringProperty(membership);
        this.program = new SimpleStringProperty(program);
        this.startDate = new SimpleObjectProperty<>(startDate);
        this.numSessions = new SimpleIntegerProperty(numSessions);
        this.totalCost = new SimpleIntegerProperty(totalCost);
    }

    // ✅ Getters
    public int getId() { return id.get(); }
    public String getFullName() { return fullName.get(); }
    public String getContact() { return contact.get(); }
    public String getMembership() { return membership.get(); }
    public String getProgram() { return program.get(); }
    public LocalDate getStartDate() { return startDate.get(); }
    public int getNumSessions() { return numSessions.get(); }
    public int getTotalCost() { return totalCost.get(); }

    // ✅ Setters
    public void setId(int id) { this.id.set(id); }
    public void setFullName(String name) { this.fullName.set(name); }
    public void setContact(String contact) { this.contact.set(contact); }
    public void setMembership(String type) { this.membership.set(type); }
    public void setProgram(String program) { this.program.set(program); }
    public void setStartDate(LocalDate date) { this.startDate.set(date); }
    public void setNumSessions(int sessions) { this.numSessions.set(sessions); }
    public void setTotalCost(int cost) { this.totalCost.set(cost); }

    // ✅ Computed field: Booking code (formatted for UI)
    public String getFormattedBookingId() {
        return String.format("B%03d", getId());
    }

    // ✅ JavaFX Properties (for bindings)
    public IntegerProperty idProperty() { return id; }
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty membershipProperty() { return membership; }
    public StringProperty programProperty() { return program; }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    public IntegerProperty numSessionsProperty() { return numSessions; }
    public IntegerProperty totalCostProperty() { return totalCost; }

    // ✅ Property for TableView to show formatted booking code
    public StringProperty bookingIdProperty() {
        return new SimpleStringProperty(getFormattedBookingId());
    }
}
