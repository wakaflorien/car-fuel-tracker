package com.carfuel.model;

import java.util.ArrayList;
import java.util.List;

public class Car {
    private int id;
    private String brand;
    private String model;
    private int year;
    private List<FuelEntry> fuelEntries;

    // Contructor, getters, setters
    public Car(int id, String brand, String model, int year) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.fuelEntries = new ArrayList<>();
    }

    // Add getters and setters for all fields
    public int getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public List<FuelEntry> getFuelEntries() {
        return fuelEntries;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setFuelEntries(List<FuelEntry> fuelEntries) {
        this.fuelEntries = fuelEntries;
    }

}
