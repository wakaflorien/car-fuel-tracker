package com.carfuel;

import com.carfuel.model.Car;
import com.carfuel.model.FuelEntry;
import com.carfuel.service.CarService;
import junit.framework.TestCase;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for Car Fuel Tracker service layer.
 */
public class AppTest extends TestCase {
    
    private CarService carService;
    
    @Override
    protected void setUp() {
        carService = new CarService();
    }
    
    /**
     * Test creating a car with valid data.
     */
    public void testCreateCar() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        
        assertNotNull("Car should not be null", car);
        assertEquals("Brand should match", "Toyota", car.getBrand());
        assertEquals("Model should match", "Corolla", car.getModel());
        assertEquals("Year should match", 2018, car.getYear());
        assertEquals("Car ID should be 1", 1, car.getId());
        assertTrue("Fuel entries should be empty initially", car.getFuelEntries().isEmpty());
    }
    
    /**
     * Test creating multiple cars and verify unique IDs.
     */
    public void testCreateMultipleCars() {
        Car car1 = carService.createCar("Toyota", "Corolla", 2018);
        Car car2 = carService.createCar("Honda", "Civic", 2020);
        Car car3 = carService.createCar("BMW", "320i", 2021);
        
        assertEquals("First car ID should be 1", 1, car1.getId());
        assertEquals("Second car ID should be 2", 2, car2.getId());
        assertEquals("Third car ID should be 3", 3, car3.getId());
        
        List<Car> allCars = carService.getAllCars();
        assertEquals("Should have 3 cars", 3, allCars.size());
    }
    
    /**
     * Test getting all cars.
     */
    public void testGetAllCars() {
        assertEquals("Initially should have no cars", 0, carService.getAllCars().size());
        
        carService.createCar("Toyota", "Corolla", 2018);
        carService.createCar("Honda", "Civic", 2020);
        
        List<Car> cars = carService.getAllCars();
        assertEquals("Should have 2 cars", 2, cars.size());
    }
    
    /**
     * Test getting car by ID when car exists.
     */
    public void testGetCarByIdExists() {
        Car createdCar = carService.createCar("Toyota", "Corolla", 2018);
        Car retrievedCar = carService.getCarById(createdCar.getId());
        
        assertNotNull("Retrieved car should not be null", retrievedCar);
        assertEquals("Car ID should match", createdCar.getId(), retrievedCar.getId());
        assertEquals("Brand should match", "Toyota", retrievedCar.getBrand());
    }
    
    /**
     * Test getting car by ID when car does not exist.
     */
    public void testGetCarByIdNotExists() {
        Car car = carService.getCarById(999);
        assertNull("Car should be null for non-existent ID", car);
    }
    
    /**
     * Test adding fuel entry to existing car.
     */
    public void testAddFuelEntry() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        FuelEntry entry = carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);
        
        assertNotNull("Fuel entry should not be null", entry);
        assertEquals("Liters should match", 40.0, entry.getLiters(), 0.001);
        assertEquals("Price should match", 52.5, entry.getPrice(), 0.001);
        assertEquals("Odometer should match", 45000.0, entry.getOdometer(), 0.001);
        assertEquals("Car should have 1 fuel entry", 1, car.getFuelEntries().size());
    }
    
    /**
     * Test adding fuel entry to non-existent car.
     */
    public void testAddFuelEntryToNonExistentCar() {
        FuelEntry entry = carService.addFuelEntry(999, 40.0, 52.5, 45000.0);
        assertNull("Fuel entry should be null for non-existent car", entry);
    }
    
    /**
     * Test adding multiple fuel entries to a car.
     */
    public void testAddMultipleFuelEntries() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);
        carService.addFuelEntry(car.getId(), 45.0, 55.0, 45500.0);
        carService.addFuelEntry(car.getId(), 43.0, 54.0, 46000.0);
        
        assertEquals("Car should have 3 fuel entries", 3, car.getFuelEntries().size());
    }
    
    /**
     * Test calculating stats with no fuel entries.
     */
    public void testCalculateStatsNoEntries() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        Map<String, Object> stats = carService.calculateStats(car.getId());
        
        assertNotNull("Stats should not be null", stats);
        assertEquals("Total fuel should be 0", 0.0, (Double) stats.get("totalFuel"), 0.001);
        assertEquals("Total cost should be 0", 0.0, (Double) stats.get("totalCost"), 0.001);
        assertEquals("Average consumption should be 0.0 L/100km", "0.0 L/100km", (String) stats.get("averageConsumption"));
        assertTrue("Should have message", stats.containsKey("message"));
    }
    
    /**
     * Test calculating stats with only one fuel entry.
     */
    public void testCalculateStatsOneEntry() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);
        Map<String, Object> stats = carService.calculateStats(car.getId());
        
        assertNotNull("Stats should not be null", stats);
        assertEquals("Total fuel should be 40", 40.0, (Double) stats.get("totalFuel"), 0.001);
        assertEquals("Total cost should be 2100", 2100.0, (Double) stats.get("totalCost"), 0.001);
        assertEquals("Average consumption should be 0.0 L/100km", "0.0 L/100km", (String) stats.get("averageConsumption"));
        assertTrue("Should have message about needing 2 entries", stats.containsKey("message"));
    }
    
    /**
     * Test calculating stats with two fuel entries (valid consumption calculation).
     */
    public void testCalculateStatsTwoEntries() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);
        carService.addFuelEntry(car.getId(), 45.0, 55.0, 45500.0);
        Map<String, Object> stats = carService.calculateStats(car.getId());
        
        assertNotNull("Stats should not be null", stats);
        assertEquals("Total fuel should be 85", 85.0, (Double) stats.get("totalFuel"), 0.001);
        assertEquals("Total cost should be 4575", 4575.0, (Double) stats.get("totalCost"), 0.001);
        
        // Consumption: 40L consumed over 500km = 8.0 L/100km
        String avgConsumption = (String) stats.get("averageConsumption");
        assertNotNull("Average consumption should not be null", avgConsumption);
        assertTrue("Average consumption should be 8.0 L/100km", avgConsumption.equals("8.0 L/100km"));
        assertEquals("Total distance should be 500", 500.0, (Double) stats.get("totalDistance"), 0.001);
    }
    
    /**
     * Test calculating stats with three fuel entries.
     */
    public void testCalculateStatsThreeEntries() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);
        carService.addFuelEntry(car.getId(), 45.0, 55.0, 45500.0);
        carService.addFuelEntry(car.getId(), 43.0, 54.0, 46000.0);
        Map<String, Object> stats = carService.calculateStats(car.getId());
        
        assertNotNull("Stats should not be null", stats);
        assertEquals("Total fuel should be 128", 128.0, (Double) stats.get("totalFuel"), 0.001);
        
        // Consumption: (40 + 45) L consumed over 1000km = 8.5 L/100km
        // Last entry (43L) is excluded as it hasn't been consumed yet
        String avgConsumption = (String) stats.get("averageConsumption");
        assertNotNull("Average consumption should not be null", avgConsumption);
        assertTrue("Average consumption should be approximately 8.5 L/100km", 
                   avgConsumption.equals("8.5 L/100km"));
        assertEquals("Total distance should be 1000", 1000.0, (Double) stats.get("totalDistance"), 0.001);
    }
    
    /**
     * Test calculating stats with entries having same odometer (invalid case).
     */
    public void testCalculateStatsSameOdometer() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);
        carService.addFuelEntry(car.getId(), 45.0, 55.0, 45000.0);
        Map<String, Object> stats = carService.calculateStats(car.getId());
        
        assertNotNull("Stats should not be null", stats);
        assertEquals("Total fuel should be 85", 85.0, (Double) stats.get("totalFuel"), 0.001);
        assertEquals("Average consumption should be 0.0 L/100km", "0.0 L/100km", (String) stats.get("averageConsumption"));
        assertTrue("Should have error message", stats.containsKey("message"));
        String message = (String) stats.get("message");
        assertTrue("Message should mention same odometer", 
                   message.contains("same odometer") || message.contains("Invalid odometer"));
    }
    
    /**
     * Test calculating stats for non-existent car.
     */
    public void testCalculateStatsNonExistentCar() {
        Map<String, Object> stats = carService.calculateStats(999);
        assertNull("Stats should be null for non-existent car", stats);
    }
    
    /**
     * Test calculating stats with entries added in reverse odometer order (should still work).
     */
    public void testCalculateStatsReverseOrder() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        // Add entries in reverse order
        carService.addFuelEntry(car.getId(), 45.0, 55.0, 45500.0);
        carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);
        Map<String, Object> stats = carService.calculateStats(car.getId());
        
        assertNotNull("Stats should not be null", stats);
        // Should still calculate correctly after sorting
        assertEquals("Total fuel should be 85", 85.0, (Double) stats.get("totalFuel"), 0.001);
        // Consumption: 40L over 500km = 8.0 L/100km
        String avgConsumption = (String) stats.get("averageConsumption");
        assertNotNull("Average consumption should not be null", avgConsumption);
        assertTrue("Average consumption should be 8.0 L/100km", avgConsumption.equals("8.0 L/100km"));
    }
    
    /**
     * Test total cost calculation with multiple entries.
     */
    public void testTotalCostCalculation() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);  // 40 * 52.5 = 2100
        carService.addFuelEntry(car.getId(), 45.0, 55.0, 45500.0);  // 45 * 55.0 = 2475
        Map<String, Object> stats = carService.calculateStats(car.getId());
        
        double expectedCost = 2100.0 + 2475.0; // 4575.0
        assertEquals("Total cost should be 4575", expectedCost, (Double) stats.get("totalCost"), 0.01);
    }
    
    /**
     * Test formatted consumption string in stats.
     */
    public void testFormattedConsumption() {
        Car car = carService.createCar("Toyota", "Corolla", 2018);
        carService.addFuelEntry(car.getId(), 40.0, 52.5, 45000.0);
        carService.addFuelEntry(car.getId(), 45.0, 55.0, 45500.0);
        Map<String, Object> stats = carService.calculateStats(car.getId());
        
        assertTrue("Should have averageConsumption", stats.containsKey("averageConsumption"));
        String avgConsumption = (String) stats.get("averageConsumption");
        assertNotNull("Average consumption should not be null", avgConsumption);
        assertTrue("Formatted string should contain 'L/100km'", avgConsumption.contains("L/100km"));
        assertTrue("Formatted string should match pattern X.X L/100km", avgConsumption.matches("\\d+\\.\\d+ L/100km"));
    }
}
