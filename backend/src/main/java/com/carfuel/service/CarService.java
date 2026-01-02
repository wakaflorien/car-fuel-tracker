package com.carfuel.service;

import com.carfuel.model.Car;
import com.carfuel.model.FuelEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarService {
    private Map<Integer, Car> cars = new HashMap<>();
    private int carIdCounter = 1;
    private int fuelIdCounter = 1;

    // CREATE CAR
    public Car createCar(String brand, String model, int year) {
        Car car = new Car(carIdCounter++, brand, model, year);
        cars.put(car.getId(), car);
        return car;
    }

    // GET ALL CARS
    public List<Car> getAllCars() {
        return new ArrayList<>(cars.values());
    }

    // GET CAR BY ID
    public Car getCarById(int id) {
        return cars.get(id);
    }

    // ADD FUEL ENTRY TO CAR
    public FuelEntry addFuelEntry(int carId, double liters, double price, double odometer) {
        Car car = cars.get(carId);
        if (car == null) {
            return null; // Or throw an exception
        }
        FuelEntry fuelEntry = new FuelEntry(fuelIdCounter++, liters, price, odometer);
        car.getFuelEntries().add(fuelEntry);
        return fuelEntry;
    }

    // CALCULATE STATS
    public Map<String, Object> calculateStats(int carId) {
        Car car = cars.get(carId);
        if (car == null) {
            return null;
        }

        List<FuelEntry> entries = car.getFuelEntries();
        if (entries.isEmpty()) {
            return Map.of(
                    "totalFuel", 0.0,
                    "totalCost", 0.0,
                    "averageConsumption", "0.0 L/100km",
                    "message", "No fuel entries found");
        }

        double totalLiters = 0;
        double totalCost = 0;

        for (FuelEntry entry : entries) {
            totalLiters += entry.getLiters();
            totalCost += entry.getPrice() * entry.getLiters();
        }

        // Calculate average consumption (requires at least 2 entries)
        if (entries.size() < 2) {
            return Map.of(
                    "totalFuel", totalLiters,
                    "totalCost", totalCost,
                    "averageConsumption", "0.0 L/100km",
                    "message", "Need at least 2 fuel entries to calculate average consumption");
        }

        // Sort entries by odometer to ensure correct calculation
        entries.sort((e1, e2) -> Double.compare(e1.getOdometer(), e2.getOdometer()));
        
        // Calculate average consumption
        // For fuel consumption: we calculate based on fuel consumed between fill-ups
        // The last fill-up's fuel hasn't been consumed yet, so we exclude it
        double consumedFuel = 0;
        double totalDistance = 0;
        int validPairs = 0;
        
        // For each pair of consecutive entries, calculate consumption
        // Entry i represents a fill-up, and we calculate how much fuel from entry i-1
        // was consumed to travel from odometer i-1 to odometer i
        for (int i = 1; i < entries.size(); i++) {
            FuelEntry previousEntry = entries.get(i - 1);
            FuelEntry currentEntry = entries.get(i);
            
            double fuelFromPreviousFill = previousEntry.getLiters();
            double distanceTraveled = currentEntry.getOdometer() - previousEntry.getOdometer();
            
            if (distanceTraveled > 0) {
                consumedFuel += fuelFromPreviousFill;
                totalDistance += distanceTraveled;
                validPairs++;
            }
        }
        
        String averageConsumption = "0.0 L/100km";
        
        if (totalDistance > 0 && validPairs > 0) {
            double avgConsumptionValue = (consumedFuel / totalDistance) * 100;
            // Format as "X.X L/100km"
            averageConsumption = String.format("%.1f L/100km", avgConsumptionValue);
        } else {
            // Check if all entries have the same odometer reading
            boolean allSameOdometer = true;
            if (entries.size() > 1) {
                double firstOdometer = entries.get(0).getOdometer();
                for (FuelEntry entry : entries) {
                    if (Math.abs(entry.getOdometer() - firstOdometer) > 0.001) {
                        allSameOdometer = false;
                        break;
                    }
                }
            }
            
            String errorMessage = allSameOdometer 
                ? "All fuel entries have the same odometer reading. Cannot calculate consumption without distance traveled."
                : "Invalid odometer readings: entries must have increasing odometer values to calculate consumption.";
            
            return Map.of(
                    "totalFuel", totalLiters,
                    "totalCost", totalCost,
                    "averageConsumption", "0.0 L/100km",
                    "message", errorMessage);
        }

        return Map.of(
                "totalFuel", totalLiters,
                "totalCost", totalCost,
                "averageConsumption", averageConsumption,
                "totalDistance", totalDistance);
    }
}
