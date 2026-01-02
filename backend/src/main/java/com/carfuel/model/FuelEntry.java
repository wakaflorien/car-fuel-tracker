package com.carfuel.model;

import java.time.LocalDateTime;

public class FuelEntry {
    private int id;
    private double liters;
    private double price;
    private double odometer;
    private LocalDateTime timestamp;

    public FuelEntry(int id, double liters, double price, double odometer) {
        this.id = id;
        this.liters = liters;
        this.price = price;
        this.odometer = odometer;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters for all fields
    public int getId() {
        return id;
    }

    public double getLiters() {
        return liters;
    }

    public double getPrice() {
        return price;
    }

    public double getOdometer() {
        return odometer;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLiters(double liters) {
        this.liters = liters;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setOdometer(double odometer) {
        this.odometer = odometer;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
