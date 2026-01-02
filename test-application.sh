#!/bin/bash

echo "=== Testing Car Fuel Tracker Application ==="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Start backend server in background
echo -e "${YELLOW}Starting backend server...${NC}"
cd backend
mvn exec:java > ../server.log 2>&1 &
SERVER_PID=$!
cd ..

# Wait for server to start
echo "Waiting for server to start..."
sleep 8

# Check if server is running
if ! curl -s http://localhost:8080/api/cars > /dev/null 2>&1; then
    echo -e "${RED}Server failed to start. Check server.log for details.${NC}"
    cat server.log
    kill $SERVER_PID 2>/dev/null
    exit 1
fi

echo -e "${GREEN}Server is running!${NC}"
echo ""

# Test 1: Create a car
echo -e "${YELLOW}Test 1: Creating a car...${NC}"
CAR_RESPONSE=$(curl -s -X POST http://localhost:8080/api/cars \
  -H "Content-Type: application/json" \
  -d '{"brand":"Toyota","model":"Corolla","year":2018}')
echo "Response: $CAR_RESPONSE"
CAR_ID=$(echo $CAR_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo -e "${GREEN}Car created with ID: $CAR_ID${NC}"
echo ""

# Test 2: List all cars
echo -e "${YELLOW}Test 2: Listing all cars...${NC}"
curl -s http://localhost:8080/api/cars | python3 -m json.tool
echo ""

# Test 3: Add fuel entry
echo -e "${YELLOW}Test 3: Adding fuel entry...${NC}"
FUEL_RESPONSE=$(curl -s -X POST http://localhost:8080/api/cars/$CAR_ID/fuel \
  -H "Content-Type: application/json" \
  -d '{"liters":40,"price":52.5,"odometer":45000}')
echo "Response: $FUEL_RESPONSE"
echo ""

# Test 4: Add another fuel entry
echo -e "${YELLOW}Test 4: Adding second fuel entry...${NC}"
FUEL_RESPONSE2=$(curl -s -X POST http://localhost:8080/api/cars/$CAR_ID/fuel \
  -H "Content-Type: application/json" \
  -d '{"liters":45,"price":55.0,"odometer":45500}')
echo "Response: $FUEL_RESPONSE2"
echo ""

# Test 5: Get fuel statistics
echo -e "${YELLOW}Test 5: Getting fuel statistics...${NC}"
STATS_RESPONSE=$(curl -s http://localhost:8080/api/cars/$CAR_ID/fuel/stats)
echo "Response: $STATS_RESPONSE"
echo "$STATS_RESPONSE" | python3 -m json.tool
echo ""

# Test 6: Test servlet endpoint
echo -e "${YELLOW}Test 6: Testing servlet endpoint...${NC}"
SERVLET_RESPONSE=$(curl -s "http://localhost:8080/servlet/fuel-stats?carId=$CAR_ID")
echo "Response: $SERVLET_RESPONSE"
echo "$SERVLET_RESPONSE" | python3 -m json.tool
echo ""

# Test 7: Test CLI
echo -e "${YELLOW}Test 7: Testing CLI - Create car...${NC}"
cd cli
CLI_OUTPUT=$(mvn exec:java -Dexec.args="create-car --brand Honda --model Civic --year 2020" 2>&1 | tail -5)
echo "$CLI_OUTPUT"
cd ..

echo ""
echo -e "${GREEN}=== All tests completed! ===${NC}"

# Stop server
echo "Stopping server..."
kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null
