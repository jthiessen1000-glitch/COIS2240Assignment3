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
		//| Plate: 1 | make: 1 | model: 1 | year: 1 | Status: Available | | Seats: 1
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
    
 // System.out.print(" ");   
 // method for loading vehicle data
    private Vehicle parseVehicle(String line) {
        String[] parts = line.split("\\|");

        // Extract common fields
        String plate = extractValue(parts[1], "Plate:");
        String make = extractValue(parts[2], "make:");
        String model = extractValue(parts[3], "model:");
        int year = Integer.parseInt(extractValue(parts[4], "year:"));
        String statusStr = extractValue(parts[5], "Status:");

        // Determine vehicle type by looking at the next part (index 7 usually)(6 is empty)
        Vehicle v = null;
        String field = parts[7].trim();
        if (field.startsWith("Seats:")) {
            int seats = Integer.parseInt(extractValue(parts[7], "Seats:"));
            v = new Car(make, model, year, seats);
            } else if (field.startsWith("Accessible:")) {
                boolean accessible = extractValue(parts[7], "Accessible:").equalsIgnoreCase("Yes");
                v = new Minibus(make, model, year, accessible);
            } else if (field.startsWith("Cargo Size:")) {
                double cargoSize = Double.parseDouble(extractValue(parts[6], "Cargo Size:"));
                boolean hasTrailer = false;
                if (parts.length > 8 && parts[8].trim().startsWith("Has Trailer:")) {
                    hasTrailer = extractValue(parts[8], "Has Trailer:").equalsIgnoreCase("Yes");
                }
                v = new PickupTruck(make, model, year, cargoSize, hasTrailer);
            }

        if (v != null) {
            v.setLicensePlate(plate);
            if (statusStr.equals("Available")) {
                v.setStatus(Vehicle.VehicleStatus.Available);
            } else if (statusStr.equals("Rented")) {
                v.setStatus(Vehicle.VehicleStatus.Rented);
            }
        }
        return v;
    }
    
    
    public boolean addVehicle(Vehicle vehicle) {
    	String plate = vehicle.getLicensePlate();

    	if (findVehicleByPlate(plate) == null) {
    		vehicles.add(vehicle);
    		saveVehicle();
    		System.out.print("vehicle added succesfully ");
        return true;
    	}
    	else {
    		System.out.print("lisences plate already exists ");
        return false;
    	}
    }

    public boolean addCustomer(Customer customer) {
    	int ID = customer.getCustomerId();

    	if (findCustomerById(ID) == null) {
    		customers.add(customer);
    		saveCustomer();
    		System.out.print("customer added succesfully ");
        return true;
    	}
    	else {
    		System.out.print("customer ID already exists ");
        return false;
    	}
    }
    

    public void saveVehicle() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("vehicles.txt"))) {
            for (Vehicle v : vehicles) {
                writer.write(v.getInfo());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving vehicles: " + e.getMessage());
        }
    }

    public void saveCustomer() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("customers.txt", true))) {
        	for (Customer v : customers) {
                writer.write(v.toString());
                writer.newLine();
            }
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
        saveVehicle();
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
        saveVehicle();
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