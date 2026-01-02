package com.carfuel.servlet;

import com.carfuel.service.CarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class FuelStatsServlet extends HttpServlet {
    private CarService carService;
    private ObjectMapper objectMapper;

    public FuelStatsServlet(CarService carService) {
        this.carService = carService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Public method to allow calling from Spark route
    public void handleGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        // Manually parse carId from query parameters
        String carIdParam = request.getParameter("carId");
        
        if (carIdParam == null || carIdParam.isEmpty()) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.print(objectMapper.writeValueAsString(Map.of("error", "carId parameter is required")));
            out.flush();
            return;
        }

        try {
            int carId = Integer.parseInt(carIdParam);
            Map<String, Object> stats = carService.calculateStats(carId);

            // Set Content-Type explicitly
            response.setContentType("application/json");
            
            if (stats == null) {
                // Set status code explicitly
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                PrintWriter out = response.getWriter();
                out.print(objectMapper.writeValueAsString(Map.of("error", "Car not found")));
                out.flush();
            } else {
                // Set status code explicitly
                response.setStatus(HttpServletResponse.SC_OK);
                PrintWriter out = response.getWriter();
                out.print(objectMapper.writeValueAsString(stats));
                out.flush();
            }
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.print(objectMapper.writeValueAsString(Map.of("error", "Invalid carId format")));
            out.flush();
        }
    }
}
