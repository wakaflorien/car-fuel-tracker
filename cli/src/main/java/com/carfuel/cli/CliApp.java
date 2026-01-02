package com.carfuel.cli;

import com.carfuel.cli.ApiClient.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Main CLI application for Car Fuel Tracker.
 * This is a separate executable/main class from the backend, communicating via HTTP.
 */
public class CliApp {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public CliApp() {
        this.apiClient = new ApiClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Main entry point for the CLI application.
     * 
     * @param args Command line arguments
     */
    public void run(String[] args) {
        if (args.length == 0) {
            CommandParser.printUsage();
            System.exit(0);
        }

        Map<String, String> params = CommandParser.parseArgs(args);
        String command = params.get("command");

        try {
            switch (command) {
                case "create-car":
                    handleCreateCar(params);
                    break;
                case "add-fuel":
                    handleAddFuel(params);
                    break;
                case "fuel-stats":
                    handleFuelStats(params);
                    break;
                default:
                    System.err.println("Error: Unknown command '" + command + "'");
                    System.err.println();
                    CommandParser.printUsage();
                    System.exit(1);
            }
        } catch (ApiException e) {
            // Handle API-specific errors with clear messages (including 404 for invalid IDs)
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            CommandParser.printUsage();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Handles the create-car command.
     * Validates parameters and creates a new car via the API.
     */
    private void handleCreateCar(Map<String, String> params) throws ApiException {
        String brand = params.get("brand");
        String model = params.get("model");
        String yearStr = params.get("year");

        // Validate required parameters
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: --brand");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: --model");
        }
        if (yearStr == null || yearStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: --year");
        }

        // Validate and parse year
        int year;
        try {
            year = Integer.parseInt(yearStr);
            if (year < 1900 || year > 2100) {
                throw new IllegalArgumentException("Year must be between 1900 and 2100");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid year format: " + yearStr);
        }

        // Create car via API
        String response = apiClient.createCar(brand.trim(), model.trim(), year);
        JsonNode car;
        try {
            car = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new ApiException("Failed to parse server response: " + e.getMessage());
        }
        
        // Display success message with car details
        System.out.println("Car created successfully!");
        System.out.println("ID: " + car.get("id").asInt());
        System.out.println("Brand: " + car.get("brand").asText());
        System.out.println("Model: " + car.get("model").asText());
        System.out.println("Year: " + car.get("year").asInt());
    }

    /**
     * Handles the add-fuel command.
     * Validates parameters and adds a fuel entry via the API.
     */
    private void handleAddFuel(Map<String, String> params) throws ApiException {
        String carIdStr = params.get("carId");
        String litersStr = params.get("liters");
        String priceStr = params.get("price");
        String odometerStr = params.get("odometer");

        // Validate required parameters
        if (carIdStr == null || carIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: --carId");
        }
        if (litersStr == null || litersStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: --liters");
        }
        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: --price");
        }
        if (odometerStr == null || odometerStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: --odometer");
        }

        // Validate and parse numeric values
        int carId;
        double liters, price, odometer;
        
        try {
            carId = Integer.parseInt(carIdStr);
            if (carId <= 0) {
                throw new IllegalArgumentException("Car ID must be a positive number");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid car ID format: " + carIdStr);
        }

        try {
            liters = Double.parseDouble(litersStr);
            if (liters <= 0) {
                throw new IllegalArgumentException("Liters must be a positive number");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid liters format: " + litersStr);
        }

        try {
            price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format: " + priceStr);
        }

        try {
            odometer = Double.parseDouble(odometerStr);
            if (odometer < 0) {
                throw new IllegalArgumentException("Odometer cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid odometer format: " + odometerStr);
        }

        // Add fuel entry via API
        apiClient.addFuel(carId, liters, price, odometer);
        System.out.println("Fuel entry added successfully!");
    }

    /**
     * Handles the fuel-stats command.
     * Retrieves and displays fuel statistics for a car via the API.
     */
    private void handleFuelStats(Map<String, String> params) throws ApiException {
        String carIdStr = params.get("carId");

        // Validate required parameter
        if (carIdStr == null || carIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: --carId");
        }

        // Validate and parse car ID
        int carId;
        try {
            carId = Integer.parseInt(carIdStr);
            if (carId <= 0) {
                throw new IllegalArgumentException("Car ID must be a positive number");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid car ID format: " + carIdStr);
        }

        // Get fuel stats via API
        String response = apiClient.getFuelStats(carId);
        JsonNode stats;
        try {
            stats = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new ApiException("Failed to parse server response: " + e.getMessage());
        }

        // Extract statistics
        double totalFuel = stats.has("totalFuel") ? stats.get("totalFuel").asDouble() : 0.0;
        double totalCost = stats.has("totalCost") ? stats.get("totalCost").asDouble() : 0.0;
        String avgConsumption = stats.has("averageConsumption") ? stats.get("averageConsumption").asText() : "0.0 L/100km";

        // Display statistics
        System.out.println("Total fuel: " + String.format("%.1f", totalFuel) + " L");
        System.out.println("Total cost: " + String.format("%.2f", totalCost));
        System.out.println("Average consumption: " + avgConsumption);
        
        // Display message if present (error cases)
        if (stats.has("message")) {
            System.out.println("Note: " + stats.get("message").asText());
        }
    }

    public static void main(String[] args) {
        CliApp app = new CliApp();
        app.run(args);
    }
}
