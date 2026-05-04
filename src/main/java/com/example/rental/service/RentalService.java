package com.example.rental.service;

import com.example.rental.model.Rental;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentalService {

    private final String FILE_PATH = "src/main/resources/data/rentals.txt";

    @PostConstruct
    public void init() {
        try {
            File file = new File(FILE_PATH);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("RentalID,UserID,BikeID,Status");
                    writer.println("R001,U001,B001,Rented");
                    writer.println("R002,U002,B002,Rented");
                    writer.println("R003,U003,B003,Rented");
                }
            }
        } catch (IOException e) {
            System.err.println("Could not initialize rentals file: " + e.getMessage());
        }
    }

    public List<Rental> getAllRentals() {
        List<Rental> rentals = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return rentals;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] data = line.split(",");
                if (data.length >= 4) {
                    rentals.add(new Rental(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rentals;
    }

    /**
     * Returns a bike based on input ID.
     * @param inputId The Rental ID or Bike ID
     * @return A status message code: "SUCCESS", "ALREADY_RETURNED", or "NOT_FOUND"
     */
    public boolean rentVehicle(String userId, String bikeId) {
        String rentalId = "R" + System.currentTimeMillis() % 10000;
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            writer.println(String.format("%s,%s,%s,%s", rentalId, userId, bikeId, "Rented"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String returnBike(String inputId, VehicleService vehicleService) {
        if (inputId == null || inputId.trim().isEmpty()) return "NOT_FOUND";
        
        File file = new File(FILE_PATH);
        File tempFile = new File(file.getParent(), "temp_rentals.txt");
        boolean foundId = false;
        boolean wasRented = false;
        String searchId = inputId.trim();
        String affectedBikeId = null;

        try (
                BufferedReader reader = new BufferedReader(new FileReader(file));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    writer.write(line);
                    writer.newLine();
                    continue;
                }
                
                if (isHeader) {
                    writer.write(line);
                    writer.newLine();
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length >= 4) {
                    String rentalId = data[0].trim();
                    String bikeId = data[2].trim();
                    String status = data[3].trim();

                    if (rentalId.equalsIgnoreCase(searchId) || bikeId.equalsIgnoreCase(searchId)) {
                        foundId = true;
                        if (!status.equalsIgnoreCase("Returned")) {
                            data[3] = "Returned";
                            line = String.join(",", data);
                            wasRented = true;
                            affectedBikeId = bikeId;
                        }
                    }
                }

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR";
        }

        if (wasRented) {
            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (affectedBikeId != null) {
                    vehicleService.updateVehicleStatus(affectedBikeId, "Available");
                }
                return "SUCCESS";
            } catch (IOException e) {
                e.printStackTrace();
                tempFile.delete();
                return "ERROR";
            }
        } else {
            tempFile.delete();
            return foundId ? "ALREADY_RETURNED" : "NOT_FOUND";
        }
    }
}
