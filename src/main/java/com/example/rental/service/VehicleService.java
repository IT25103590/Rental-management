package com.example.rental.service;

import com.example.rental.model.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VehicleService {
    private final String FILE_PATH = "src/main/resources/data/vehicles.txt";
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @PostConstruct
    public void init() {
        try {
            File file = new File(FILE_PATH);
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            if (!file.exists()) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("VehicleID,Model,Type,Status,DailyRate,Description,ImageUrl,Kilometers");
                    writer.println("B001,Giant Escape,Bike,Rented,15.0,Standard commuter bike,https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&q=80&w=400,1250.5");
                    writer.println("B002,Trek Marlin,Bike,Rented,20.0,Mountain bike for trails,https://images.unsplash.com/photo-1532298229144-0ee0557ff62a?auto=format&fit=crop&q=80&w=400,840.2");
                    writer.println("B003,Specialized Sirrus,Bike,In Service,18.0,High performance city bike,https://images.unsplash.com/photo-1571068316344-75bc76f77891?auto=format&fit=crop&q=80&w=400,2100.0");
                    writer.println("B004,Cannondale Quick,Bike,Available,17.0,Lightweight fitness bike,https://images.unsplash.com/photo-1507035895480-2b3156c31fc8?auto=format&fit=crop&q=80&w=400,150.7");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return vehicles;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                List<String> data = parseCsvLine(line);
                if (data.size() >= 6) {
                    vehicles.add(new Vehicle(
                            data.get(0),
                            data.get(1),
                            data.get(2),
                            data.get(3),
                            Double.parseDouble(data.get(4)),
                            data.get(5),
                            data.size() > 6 ? data.get(6) : "",
                            data.size() > 7 ? Double.parseDouble(data.get(7)) : 0.0
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;
        char[] chars = line.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < chars.length && chars[i + 1] == '\"') {
                        curVal.append('\"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    curVal.append(ch);
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    result.add(curVal.toString().trim());
                    curVal.setLength(0);
                } else {
                    curVal.append(ch);
                }
            }
        }
        result.add(curVal.toString().trim());
        return result;
    }

    public Vehicle getVehicleById(String id) {
        return getAllVehicles().stream()
                .filter(v -> v.getVehicleId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public boolean registerVehicle(Vehicle vehicle) {
        List<Vehicle> all = getAllVehicles();
        all.add(vehicle);
        return saveAll(all);
    }

    public boolean updateVehicle(Vehicle updated) {
        List<Vehicle> all = getAllVehicles();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getVehicleId().equalsIgnoreCase(updated.getVehicleId())) {
                all.set(i, updated);
                found = true;
                break;
            }
        }
        if (found) {
            return saveAll(all);
        }
        return false;
    }

    public boolean deleteVehicle(String id) {
        List<Vehicle> all = getAllVehicles();
        List<Vehicle> filtered = all.stream()
                .filter(v -> !v.getVehicleId().equalsIgnoreCase(id))
                .collect(Collectors.toList());
        if (filtered.size() < all.size()) {
            return saveAll(filtered);
        }
        return false;
    }

    public boolean updateVehicleStatus(String vehicleId, String status) {
        List<Vehicle> all = getAllVehicles();
        for (Vehicle v : all) {
            if (v.getVehicleId().equalsIgnoreCase(vehicleId)) {
                v.setStatus(status);
                return saveAll(all);
            }
        }
        return false;
    }

    private boolean saveAll(List<Vehicle> list) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            writer.println("VehicleID,Model,Type,Status,DailyRate,Description,ImageUrl,Kilometers");
            for (Vehicle v : list) {
                writer.println(String.format("%s,%s,%s,%s,%.2f,%s,%s,%.2f",
                        escapeCsv(v.getVehicleId()),
                        escapeCsv(v.getModel()),
                        escapeCsv(v.getType()),
                        escapeCsv(v.getStatus()),
                        v.getDailyRate(),
                        escapeCsv(v.getTechnicalDescription()),
                        escapeCsv(v.getImageUrl() != null ? v.getImageUrl() : ""),
                        v.getRunningKilometer()));
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved to: " + path.toAbsolutePath());
            return "uploads/" + fileName;
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
