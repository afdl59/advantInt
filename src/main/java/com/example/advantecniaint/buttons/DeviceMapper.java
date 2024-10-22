package com.example.advantecniaint.buttons;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the mapping between beds and bays.
 */
public class DeviceMapper {
    private final Map<String, Integer> bedToBayMap = new HashMap<>();
    private final Map<Integer, String> bayToBedMap = new HashMap<>();
    private final Map<String, String> ipToBedMap = new HashMap<>();
    private final Map<String, String> bayToIpMap = new HashMap<>();

    public DeviceMapper(String csvFilePath) {
        loadMappingFromCSV(csvFilePath);
    }

    private void loadMappingFromCSV(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip the header line
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String ip = parts[0];
                    String bed = parts[1] + "/" + parts[2] + "/" + parts[3];  // Format: circuit/type/bed
                    int bay = Integer.parseInt(parts[4]);
                    String ipBedKey = ip + "-" + bed; // Unique key combining IP and bed identifier
                    bedToBayMap.put(ipBedKey, bay);
                    bayToBedMap.put(bay, ipBedKey);
                    ipToBedMap.put(ip, bed);
                    bayToIpMap.put(String.valueOf(bay), ip);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading mapping file: " + e.getMessage());
        }
    }

    public Integer getBayForBed(String ip, String bed) {
        return bedToBayMap.get(ip + "-" + bed);
    }

    public String getBedForBay(String bay) {
        return bayToBedMap.get(Integer.parseInt(bay));
    }

    public String getBedForIP(String ip) {
        return ipToBedMap.get(ip);
    }

    public String getIpForBay(String bay) {
        return bayToIpMap.get(bay);
    }

    public Map<String, String> getIpToBedMap() {
        return ipToBedMap;
    }
}
