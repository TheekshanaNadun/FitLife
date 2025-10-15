package com.fitlife.model;

import javafx.beans.property.*;

public class Program {

    private final StringProperty id;
    private final StringProperty name;
    private final IntegerProperty costPerSession;
    private final StringProperty description;
    private final StringProperty trainer;

    // ✅ Constructor
    public Program(String id, String name, int costPerSession, String description, String trainer) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.costPerSession = new SimpleIntegerProperty(costPerSession);
        this.description = new SimpleStringProperty(description);
        this.trainer = new SimpleStringProperty(trainer);
    }

    // ✅ Getters
    public String getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public int getCostPerSession() {
        return costPerSession.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getTrainer() {
        return trainer.get();
    }

    // ✅ Setters
    public void setId(String id) {
        this.id.set(id);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setCostPerSession(int costPerSession) {
        this.costPerSession.set(costPerSession);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setTrainer(String trainer) {
        this.trainer.set(trainer);
    }

    // ✅ Property Methods (for TableView binding)
    public StringProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public IntegerProperty costPerSessionProperty() {
        return costPerSession;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty trainerProperty() {
        return trainer;
    }

    // ✅ (NEW) Helper — case-insensitive match for search
    public boolean matchesSearch(String query) {
        if (query == null || query.isEmpty()) return true;
        String lower = query.toLowerCase();
        return (getName() != null && getName().toLowerCase().contains(lower))
                || (getTrainer() != null && getTrainer().toLowerCase().contains(lower))
                || (getDescription() != null && getDescription().toLowerCase().contains(lower));
    }

    // ✅ Optional — nicer toString() for debugging/logs
    @Override
    public String toString() {
        return String.format("Program{id='%s', name='%s', cost=%d, trainer='%s'}",
                getId(), getName(), getCostPerSession(), getTrainer());
    }
}
