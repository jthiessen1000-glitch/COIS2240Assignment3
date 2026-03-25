import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

class VehicleRentalTest {

    private static class TestVehicle extends Vehicle{
        public TestVehicle(String make, String model, int year) {
            super(make, model, year);
        }
    }
    
    
    //test lisence plate validity
    @Test
    public void testLicensePlate() {
        // Test valid license plates
        String[] validPlates = {"AAA100", "ABC567", "ZZZ999"};
        for (String plate : validPlates) {
            Vehicle vehicle = new TestVehicle("Toyota", "Camry", 2020);
            vehicle.setLicensePlate(plate);
            // Verify the plate was set correctly (uppercase)
            assertTrue(vehicle.getLicensePlate().equals(plate.toUpperCase()));
            // Additional assertion: plate is not null (true that it is not null)
            assertTrue(vehicle.getLicensePlate() != null);
        }

        // Test invalid license plates
        String[] invalidPlates = {"", null, "AAA1000", "ZZZ99"};
        for (String plate : invalidPlates) {
            Vehicle vehicle = new TestVehicle("Honda", "Civic", 2019);
            // Expect IllegalArgumentException when setting an invalid plate
            assertThrows(IllegalArgumentException.class, () -> vehicle.setLicensePlate(plate));
            // After the exception, the license plate should remain null (or unchanged)
            assertFalse(vehicle.getLicensePlate() != null);
        }
    }
    
    @Test
    public void testRentAndReturnVehicle() {
        // Instantiate Vehicle and Customer objects
        Vehicle car = new Car("Toyota", "Camry", 2022, 5);
        car.setLicensePlate("ABC123");          // Set a valid plate for completeness
        Customer customer = new Customer(1001, "John Doe");

        //make sure vehicle is initially available
        assertEquals(Vehicle.VehicleStatus.Available, car.getStatus());

        //the single RentalSystem instance
        RentalSystem rentalSystem = RentalSystem.getInstance();

        // Rent the vehicle first time
        boolean firstRent = rentalSystem.rentVehicle(car, customer, LocalDate.now(), 1.0);
        assertTrue(firstRent);
        assertEquals(Vehicle.VehicleStatus.Rented, car.getStatus());

        // rent the same vehicle again (should fail)
        boolean secondRent = rentalSystem.rentVehicle(car, customer, LocalDate.now(), 1.0);
        assertFalse(secondRent);

        // Return the vehicle
        boolean firstReturn = rentalSystem.returnVehicle(car, customer, LocalDate.now(), 100.5);
        assertTrue(firstReturn);
        assertEquals(Vehicle.VehicleStatus.Available, car.getStatus());

        // returning the same vehicle again
        boolean secondReturn = rentalSystem.returnVehicle(car, customer,LocalDate.now(), 100.5);
        assertFalse(secondReturn);
    }
    
    
    
    
}

    