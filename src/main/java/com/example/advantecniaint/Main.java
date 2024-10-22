package com.example.advantecniaint;

import com.example.advantecniaint.buttons.DeviceMapper;
import com.example.advantecniaint.buttons.ButtonManager;

public class Main {
    public static void main(String[] args) {
        String csvFilePath = "path/to/your/csvfile.csv";
        int port = 8080; // Replace with your actual port

        // Load device mappings
        DeviceMapper deviceMapper = new DeviceMapper(csvFilePath);

        // Initialize ButtonManager
        ButtonManager buttonManager = new ButtonManager(port, deviceMapper);

        // Start connections to buttons
        buttonManager.initializeButtons();
    }
}
