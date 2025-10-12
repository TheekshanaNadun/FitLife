package com.fitlife.model;

import javafx.beans.property.*;

public class Program {
    private final IntegerProperty id;
    private final StringProperty name;
    private final IntegerProperty duration;
    private final DoubleProperty price;

    public Program(int id, String name, int duration, double price) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.duration = new SimpleIntegerProperty(duration);
        this.price = new SimpleDoubleProperty(price);
    }

    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public int getDuration() { return duration.get(); }
    public double getPrice() { return price.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty durationProperty() { return duration; }
    public DoubleProperty priceProperty() { return price; }
}
