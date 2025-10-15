package com.fitlife.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Booking {

    private final StringProperty bookingId;
    private final StringProperty fullName;
    private final StringProperty contactNumber;
    private final StringProperty membershipType;
    private final StringProperty program;
    private final ObjectProperty<LocalDate> startDate;
    private final IntegerProperty sessions;
    private final IntegerProperty totalCost;

    // ✅ Constructor
    public Booking(String bookingId, String fullName, String contactNumber, String membershipType,
                   String program, LocalDate startDate, int sessions, int totalCost) {
        this.bookingId = new SimpleStringProperty(bookingId);
        this.fullName = new SimpleStringProperty(fullName);
        this.contactNumber = new SimpleStringProperty(contactNumber);
        this.membershipType = new SimpleStringProperty(membershipType);
        this.program = new SimpleStringProperty(program);
        this.startDate = new SimpleObjectProperty<>(startDate);
        this.sessions = new SimpleIntegerProperty(sessions);
        this.totalCost = new SimpleIntegerProperty(totalCost);
    }

    // ✅ Getters
    public String getBookingId() { return bookingId.get(); }
    public String getFullName() { return fullName.get(); }
    public String getContactNumber() { return contactNumber.get(); }
    public String getMembershipType() { return membershipType.get(); }
    public String getProgram() { return program.get(); }
    public LocalDate getStartDate() { return startDate.get(); }
    public int getSessions() { return sessions.get(); }
    public int getTotalCost() { return totalCost.get(); }

    // ✅ Setters
    public void setBookingId(String id) { this.bookingId.set(id); }
    public void setFullName(String name) { this.fullName.set(name); }
    public void setContactNumber(String contact) { this.contactNumber.set(contact); }
    public void setMembershipType(String membership) { this.membershipType.set(membership); }
    public void setProgram(String program) { this.program.set(program); }
    public void setStartDate(LocalDate date) { this.startDate.set(date); }
    public void setSessions(int sessions) { this.sessions.set(sessions); }
    public void setTotalCost(int cost) { this.totalCost.set(cost); }

    // ✅ Property methods (for TableView binding)
    public StringProperty bookingIdProperty() { return bookingId; }
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty contactNumberProperty() { return contactNumber; }
    public StringProperty membershipTypeProperty() { return membershipType; }
    public StringProperty programProperty() { return program; }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    public IntegerProperty sessionsProperty() { return sessions; }
    public IntegerProperty totalCostProperty() { return totalCost; }
}
