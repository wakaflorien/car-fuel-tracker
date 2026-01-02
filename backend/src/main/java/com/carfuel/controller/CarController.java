package com.carfuel.controller;

import com.carfuel.model.Car;
import com.carfuel.model.FuelEntry;
import com.carfuel.service.CarService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static spark.Spark.*;
import java.util.Map;

public class CarController {
    private CarService carService;
    private ObjectMapper objectMapper;

    public CarController(CarService carService) {
        this.carService = carService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Methods to handle HTTP requests would go here
    public void initRoutes() {
        post("/api/cars", (req, res) -> {
            res.type("application/json");

            // Parse JSON from request
            Map<String, Object> carData = objectMapper.readValue(
                req.body(), 
                new TypeReference<Map<String, Object>>() {}
            );
            String brand = (String) carData.get("brand");
            String model = (String) carData.get("model");
            // Handle year as Number (could be Integer or Long from JSON)
            Number yearNum = (Number) carData.get("year");
            int year = yearNum != null ? yearNum.intValue() : 0;

            if (brand == null || model == null || year == 0) {
                res.status(400);
                return objectMapper.writeValueAsString(Map.of("error", "Missing required fields: brand, model, year"));
            }

            Car car = carService.createCar(brand, model, year);
            return objectMapper.writeValueAsString(car);
        });

        get("/api/cars", (req, res) -> {
            res.type("application/json");
            return objectMapper.writeValueAsString(carService.getAllCars());
        });

        get("/api/cars/:id", (req, res) -> {
            res.type("application/json");
            int carId = Integer.parseInt(req.params(":id"));
            Car car = carService.getCarById(carId);
            if (car == null) {
                res.status(404);
                return objectMapper.writeValueAsString(Map.of("error", "Car not found"));
            }
            return objectMapper.writeValueAsString(car);
        });

        post("/api/cars/:id/fuel", (req, res) -> {
            res.type("application/json");
            int carId = Integer.parseInt(req.params(":id"));
            Map<String, Object> fuelData = objectMapper.readValue(
                req.body(), 
                new TypeReference<Map<String, Object>>() {}
            );

            // Handle numbers that might come as Double, Integer, or Long from JSON
            Number litersNum = (Number) fuelData.get("liters");
            Number priceNum = (Number) fuelData.get("price");
            Number odometerNum = (Number) fuelData.get("odometer");

            if (litersNum == null || priceNum == null || odometerNum == null) {
                res.status(400);
                return objectMapper.writeValueAsString(Map.of("error", "Missing required fields: liters, price, odometer"));
            }

            double liters = litersNum.doubleValue();
            double price = priceNum.doubleValue();
            double odometer = odometerNum.doubleValue();

            FuelEntry fuelEntry = carService.addFuelEntry(carId, liters, price, odometer);

            if (fuelEntry == null) {
                res.status(404);
                return objectMapper.writeValueAsString(Map.of("error", "Car not found"));
            }
            return objectMapper.writeValueAsString(fuelEntry);
        });

        get("/api/cars/:id/fuel/stats", (req, res) -> {
            res.type("application/json");
            int carId = Integer.parseInt(req.params(":id"));
            Map<String, Object> stats = carService.calculateStats(carId);

            if (stats == null) {
                res.status(404);
                return objectMapper.writeValueAsString(Map.of("error", "Car not found"));
            }
            return objectMapper.writeValueAsString(stats);
        });

        exception(Exception.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            try {
                res.body(objectMapper.writeValueAsString(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal server error")));
            } catch (Exception ex) {
                res.body("{\"error\":\"Internal server error\"}");
            }
        });
    }
}
