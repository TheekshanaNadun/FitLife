package com.fitlife.model;

import javafx.beans.property.*;

public class Staff {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty gender;
    private final StringProperty role;
    private final StringProperty contact;
    private final StringProperty email;
    private final IntegerProperty salary;

    public Staff(String id, String name, String gender, String role, String contact, String email, int salary) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.gender = new SimpleStringProperty(gender);
        this.role = new SimpleStringProperty(role);
        this.contact = new SimpleStringProperty(contact);
        this.email = new SimpleStringProperty(email);
        this.salary = new SimpleIntegerProperty(salary);
    }

    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty roleProperty() { return role; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty emailProperty() { return email; }
    public IntegerProperty salaryProperty() { return salary; }
}
