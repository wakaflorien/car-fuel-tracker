package com.carfuel.cli;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for communicating with the Car Fuel Tracker backend API.
 * Handles all HTTP requests and provides proper error handling for different status codes.
 */
public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080";
    private static final int CONNECTION_TIMEOUT_SECONDS = 5;
    
    private final HttpClient httpClient;

    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Creates a new car via the REST API.
     * 
     * @param brand Car brand
     * @param model Car model
     * @param year Car year
     * @return JSON response string
     * @throws ApiException if the request fails
     */
    public String createCar(String brand, String model, int year) throws ApiException {
        String jsonBody = String.format(
                "{\"brand\":\"%s\",\"model\":\"%s\",\"year\":%d}",
                brand, model, year);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/cars"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return handleResponse(response, "Failed to create car");
        } catch (ConnectException | java.net.http.HttpConnectTimeoutException e) {
            throw new ApiException("Cannot connect to server. Please ensure the backend server is running on " + BASE_URL);
        } catch (IOException e) {
            throw new ApiException("Network error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiException("Error creating car: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a fuel entry to a car via the REST API.
     * 
     * @param carId Car ID
     * @param liters Fuel liters
     * @param price Price per liter
     * @param odometer Odometer reading
     * @return JSON response string
     * @throws ApiException if the request fails (including 404 for car not found)
     */
    public String addFuel(int carId, double liters, double price, double odometer) throws ApiException {
        String jsonBody = String.format(
                "{\"liters\":%.2f,\"price\":%.2f,\"odometer\":%.2f}",
                liters, price, odometer);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/cars/" + carId + "/fuel"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 404) {
                throw new ApiException("Car not found with ID: " + carId + " (404 Not Found)");
            }
            
            return handleResponse(response, "Failed to add fuel entry");
        } catch (ConnectException | java.net.http.HttpConnectTimeoutException e) {
            throw new ApiException("Cannot connect to server. Please ensure the backend server is running on " + BASE_URL);
        } catch (ApiException e) {
            throw e; // Re-throw ApiException as-is
        } catch (IOException e) {
            throw new ApiException("Network error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiException("Error adding fuel entry: " + e.getMessage(), e);
        }
    }

    /**
     * Gets fuel statistics for a car via the REST API.
     * 
     * @param carId Car ID
     * @return JSON response string
     * @throws ApiException if the request fails (including 404 for car not found)
     */
    public String getFuelStats(int carId) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/cars/" + carId + "/fuel/stats"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 404) {
                throw new ApiException("Car not found with ID: " + carId + " (404 Not Found)");
            }
            
            return handleResponse(response, "Failed to get fuel stats");
        } catch (ConnectException | java.net.http.HttpConnectTimeoutException e) {
            throw new ApiException("Cannot connect to server. Please ensure the backend server is running on " + BASE_URL);
        } catch (ApiException e) {
            throw e; // Re-throw ApiException as-is
        } catch (IOException e) {
            throw new ApiException("Network error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiException("Error getting fuel stats: " + e.getMessage(), e);
        }
    }

    /**
     * Handles HTTP response and throws appropriate exceptions for error status codes.
     * 
     * @param response HTTP response
     * @param errorMessage Base error message
     * @return Response body if status is 200
     * @throws ApiException if status code indicates an error
     */
    private String handleResponse(HttpResponse<String> response, String errorMessage) throws ApiException {
        int statusCode = response.statusCode();
        
        if (statusCode == 200) {
            return response.body();
        }
        
        // Handle different HTTP status codes
        switch (statusCode) {
            case 400:
                throw new ApiException(errorMessage + " - Bad Request (400): " + response.body());
            case 404:
                throw new ApiException(errorMessage + " - Not Found (404): " + response.body());
            case 500:
                throw new ApiException(errorMessage + " - Internal Server Error (500): " + response.body());
            default:
                throw new ApiException(errorMessage + " - HTTP " + statusCode + ": " + response.body());
        }
    }

    /**
     * Custom exception for API-related errors.
     * Provides clear error messages for different failure scenarios.
     */
    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
