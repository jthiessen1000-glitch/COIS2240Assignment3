import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;

public class RentalSystem {
	
	// holds the instance
	private static class SingleInstance{
		private static final RentalSystem INSTANCE = new RentalSystem();
	}
	
	//getter for the instance
	public static RentalSystem getInstance() {
		return SingleInstance.INSTANCE;
	}
	
	//constructor
	private RentalSystem() {
        vehicles = new ArrayList<>();
        customers = new ArrayList<>();
        rentalHistory = new RentalHistory();
        loadData();
	}
	
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();

    
    
    private void loadData(){

		try (BufferedReader reader = new BufferedReader(new FileReader("customers.txt"))) {
            String cline;
            while ((cline = reader.readLine()) != null) {
                cline = cline.trim();
                if (cline.isEmpty()) continue;

                // Expected format: "Customer ID: 32 | Name: hi"
                String[] parts = cline.split("\\|");
                if (parts.length != 2) continue;

                String idPart = parts[0].trim();
                String namePart = parts[1].trim();

                if (idPart.startsWith("Customer ID:") || namePart.startsWith("Name:"));

                try {
                    int id = Integer.parseInt(idPart.substring("Customer ID:".length()).trim());
                    String name = namePart.substring("Name:".length()).trim();
                    customers.add(new Customer(id, name));
                } catch (NumberFormatException e) {
                    // Skip malformed line
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
	    
		 
		 // Load vehicles from vehicles.txt
		    try (BufferedReader reader = new BufferedReader(new FileReader("vehicles.txt"))) {
		        String line;
		        while ((line = reader.readLine()) != null) {
		            line = line.trim();
		            if (line.isEmpty()) continue;
		            Vehicle v = parseVehicle(line);
		            if (v != null) {
		                vehicles.add(v);
		            }
		        }
		    } catch (IOException e) {
		        System.err.println("Error loading vehicles: " + e.getMessage());
		    }
		    
		    try (BufferedReader reader = new BufferedReader(new FileReader("rental_records.txt"))) {
		        String line;
		        while ((line = reader.readLine()) != null) {
		            line = line.trim();
		            if (line.isEmpty()) continue;

		            // Expected format: "RENT | Plate: 1 | Customer: bob | Date: 2026-03-24 | Amount: $52.0"
		            String[] parts = line.split("\\|");
		            if (parts.length != 5) continue;

		            String type = parts[0].trim();
		            String platePart = parts[1].trim();
		            String custPart = parts[2].trim();
		            String datePart = parts[3].trim();
		            String amountPart = parts[4].trim();

		            if (platePart.startsWith("Plate:") && custPart.startsWith("Customer:") &&
		                datePart.startsWith("Date:") && amountPart.startsWith("Amount:")) {

		                String plate = platePart.substring("Plate:".length()).trim();
		                String customerName = custPart.substring("Customer:".length()).trim();
		                String dateStr = datePart.substring("Date:".length()).trim();
		                String amountStr = amountPart.substring("Amount:".length()).trim();

		                // Remove leading '$' if present
		                if (amountStr.startsWith("$")) {
		                    amountStr = amountStr.substring(1);
		                }

		                try {
		                    LocalDate date = LocalDate.parse(dateStr);
		                    double amount = Double.parseDouble(amountStr);

		                    Vehicle vehicle = findVehicleByPlate(plate);
		                    Customer customer = findCustomerByName(customerName);
		                    if (vehicle != null && customer != null) {
		                        RentalRecord record = new RentalRecord(vehicle, customer, date, amount, type);
		                        rentalHistory.addRecord(record);
		                    }
		                } catch (Exception e) {
		                    // Skip malformed record
		                }
		            }
		        }
		    } catch (IOException e) {
		        System.err.println("Error loading rental records: " + e.getMessage());
		    }
		}
    
    
    private String extractValue(String part, String label) {
        String trimmed = part.trim();
        if (trimmed.startsWith(label)) {
            return trimmed.substring(label.length()).trim();
        }
        return "";
    }
    
    
 // method for loading vehicle data
 	private Vehicle parseVehicle(String line) {
// 		System.out.print(line);
// 		System.out.print("\n");
 	    String[] parts = line.split("\\|");
 	    if (parts.length < 6) return null; // Need at least common fields + empty separator

 	    // Extract common fields (indices 0-4)
 
 	    String plate = extractValue(parts[1], "Plate:");
 	    String make = extractValue(parts[2], "make:");
 	    String model = extractValue(parts[3], "model:");
	   System.out.print("0" + parts[0]);
	  System.out.print("1" + parts[1]);
   System.out.print("2" + parts[2]);
 	    System.out.print("3" + parts[3]);
	   System.out.print("4" + parts[4]);
	  System.out.print("5" + parts[5]);
System.out.print("6" + parts[6]);
	System.out.print("7" + parts[7]);
//	System.out.print("\n");
//	
//	System.out.print( parts);
	
 	    int year = Integer.parseInt(extractValue(parts[4], "year:"));
 	    String statusStr = extractValue(parts[5], "status:");

 	    // The remaining parts (index 6 is empty, then type-specific fields)
 	    // parts[6] onward
 	    if (parts.length < 6) return null;

 	    // Determine vehicle type by looking at the next part(s)
 	    String field1 = parts[7].trim();
 	    if (field1.startsWith("Seats:")) {
 	        // Car
 	        int seats = Integer.parseInt(extractValue(parts[7], "Seats:"));
 	        Car car = new Car(make, model, year, seats);
 	        car.setLicensePlate(plate);
 	        car.setStatus(statusStr.equalsIgnoreCase("Rented") ?
 	              Vehicle.VehicleStatus.Rented : Vehicle.VehicleStatus.Available);
 	        
 	        return car;
 	    } else if (field1.startsWith("Accessible:")) {
 	        // Minibus
 	        boolean accessible = extractValue(parts[7], "Accessible:").equalsIgnoreCase("Yes");
 	        Minibus minibus = new Minibus(make, model, year, accessible);
 	        minibus.setLicensePlate(plate);
 	        minibus.setStatus(statusStr.equalsIgnoreCase("Rented") ?
 	              Vehicle.VehicleStatus.Rented : Vehicle.VehicleStatus.Available);
	        
 	        return minibus;
 	    } else if (field1.startsWith("Cargo Size:")) {
 	        // PickupTruck – may have a second field for trailer
 	        double cargoSize = Double.parseDouble(extractValue(parts[7], "Cargo Size:"));
 	        boolean hasTrailer = false;
 	        if (parts.length >= 8) {
 	            String field2 = parts[8].trim();
 	            if (field2.startsWith("Has Trailer:")) {
 	                hasTrailer = extractValue(parts[8], "Has Trailer:").equalsIgnoreCase("Yes");
 	            }
 	        }
 	        PickupTruck truck = new PickupTruck(make, model, year, cargoSize, hasTrailer);
 	       truck.setLicensePlate(plate);
 	      truck.setStatus(statusStr.equalsIgnoreCase("Rented") ?
 	             Vehicle.VehicleStatus.Rented : Vehicle.VehicleStatus.Available);
 	        return truck;
 	    } else {
 	        return null;
 	    }
 	   
 	}
    
    
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        saveVehicle(vehicle);
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
        saveCustomer(customer);
    }
    

    public void saveVehicle(Vehicle vehicle) {
        // Append vehicle details to vehicles.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("vehicles.txt", true))) {
            writer.write(vehicle.getInfo());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving vehicle: " + e.getMessage());
        }
    }

    public void saveCustomer(Customer customer) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("customers.txt", true))) {
            writer.write(customer.toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving vehicle: " + e.getMessage());
        }
    }
    
    
    public void saveRecords(RentalRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("rental_records.txt", true))) {
            writer.write(record.toString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving vehicle: " + e.getMessage());
        }
    }
    
    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Available) {
            vehicle.setStatus(Vehicle.VehicleStatus.Rented);
            rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, amount, "RENT"));
            System.out.println("Vehicle rented to " + customer.getCustomerName());
            

        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
        RentalRecord record = new RentalRecord(vehicle, customer, date, amount, "RENT");
        saveRecords(record);

    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Rented) {
            vehicle.setStatus(Vehicle.VehicleStatus.Available);
            rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, extraFees, "RETURN"));
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
        RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
        saveRecords(record);
    }    

    public void displayVehicles(Vehicle.VehicleStatus status) {
        // Display appropriate title based on status
        if (status == null) {
            System.out.println("\n=== All Vehicles ===");
        } else {
            System.out.println("\n=== " + status + " Vehicles ===");
        }
        
        // Header with proper column widths
        System.out.printf("|%-16s | %-12s | %-12s | %-12s | %-6s | %-18s |%n", 
            " Type", "Plate", "Make", "Model", "Year", "Status");
        System.out.println("|--------------------------------------------------------------------------------------------|");
    	  
        boolean found = false;
        for (Vehicle vehicle : vehicles) {
            if (status == null || vehicle.getStatus() == status) {
                found = true;
                String vehicleType;
                if (vehicle instanceof Car) {
                    vehicleType = "Car";
                } else if (vehicle instanceof Minibus) {
                    vehicleType = "Minibus";
                } else if (vehicle instanceof PickupTruck) {
                    vehicleType = "Pickup Truck";
                } else {
                    vehicleType = "Unknown";
                }
                System.out.printf("| %-15s | %-12s | %-12s | %-12s | %-6d | %-18s |%n", 
                    vehicleType, vehicle.getLicensePlate(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getStatus().toString());
            }
        }
        if (!found) {
            if (status == null) {
                System.out.println("  No Vehicles found.");
            } else {
                System.out.println("  No vehicles with Status: " + status);
            }
        }
        System.out.println();
    }

    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        if (rentalHistory.getRentalHistory().isEmpty()) {
            System.out.println("  No rental history found.");
        } else {
            // Header with proper column widths
            System.out.printf("|%-10s | %-12s | %-20s | %-12s | %-12s |%n", 
                " Type", "Plate", "Customer", "Date", "Amount");
            System.out.println("|-------------------------------------------------------------------------------|");
            
            for (RentalRecord record : rentalHistory.getRentalHistory()) {                
                System.out.printf("| %-9s | %-12s | %-20s | %-12s | $%-11.2f |%n", 
                    record.getRecordType(), 
                    record.getVehicle().getLicensePlate(),
                    record.getCustomer().getCustomerName(),
                    record.getRecordDate().toString(),
                    record.getTotalAmount()
                );
            }
            System.out.println();
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(int id) {
        for (Customer c : customers)
            if (c.getCustomerId() == id)
                return c;
        return null;
    }


private Customer findCustomerByName(String name) {
    for (Customer c : customers) {
        if (c.getCustomerName().equalsIgnoreCase(name)) {
            return c;
        }
    }
    return null;
}
}