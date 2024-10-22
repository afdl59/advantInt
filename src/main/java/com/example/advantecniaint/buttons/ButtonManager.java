package com.example.advantecniaint.buttons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages the initialization and handling of button connections.
 */
public class ButtonManager {
    private final int port;
    private final DeviceMapper deviceMapper;
    private final Map<String, Socket> connections;
    private final ScheduledExecutorService executor;

    private static final int CONNECTION_TIMEOUT = 5000; // milliseconds
    private static final int MAX_RETRIES = 5;
    private static final int RECONNECT_DELAY = 5000; // milliseconds
    private static final int HEARTBEAT_INTERVAL = 5; // seconds

    public ButtonManager(int port, DeviceMapper deviceMapper) {
        this.port = port;
        this.deviceMapper = deviceMapper;
        this.connections = new HashMap<>();
        this.executor = Executors.newScheduledThreadPool(10); // Use a scheduled thread pool
    }

    /**
     * Initializes button connections by reading IP addresses from the CSV file and creating Socket instances.
     */
    public void initializeButtons() {
        for (String ip : deviceMapper.getIpToBedMap().keySet()) {
            connectToButton(ip);
        }
    }

    /**
     * Connects to a button and initializes a Socket instance for communication.
     */
    private void connectToButton(String ip) {
        if (connections.containsKey(ip)) {
            System.out.println("Already connected to " + ip);
            return;
        }

        executor.submit(() -> {
            boolean connected = false;
            int retries = 0;
            while (!connected && retries < MAX_RETRIES) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), CONNECTION_TIMEOUT);
                    connections.put(ip, socket);
                    startListening(socket, ip);
                    sendHeartbeats(socket, ip);
                    connected = true;
                } catch (Exception e) {
                    retries++;
                    System.err.println("Failed to connect to " + ip + ":" + port + ", attempt " + retries);
                    try {
                        Thread.sleep(RECONNECT_DELAY);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    /**
     * Starts listening to messages from the button.
     */
    private void startListening(Socket socket, String ip) {
        executor.submit(() -> {
            try (InputStream inputStream = socket.getInputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    String data = new String(buffer, 0, bytesRead);
                    System.out.println("Received data from " + ip + ": " + data);
                    storeReceivedData(ip, data); // Store received data
                }
            } catch (IOException e) {
                System.err.println("Error receiving data: " + e.getMessage());
                reconnect(socket, ip);
            }
        });
    }

    /**
     * Sends messages to the button.
     */
    public synchronized void sendMessage(String ip, String message) throws IOException {
        Socket socket = connections.get(ip);
        if (socket != null) {
            OutputStream outputStream = socket.getOutputStream();
            byte[] data = message.getBytes();
            outputStream.write(data);
            outputStream.flush();
            System.out.println("Sent message to " + ip + ": " + message);
        } else {
            System.err.println("No connection to " + ip);
        }
    }

    /**
     * Sends heartbeats to maintain connection.
     */
    private void sendHeartbeats(Socket socket, String ip) {
        Runnable heartbeatTask = () -> {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(0); // heartbeat
                outputStream.flush();
            } catch (IOException e) {
                System.err.println("Failed to send heartbeat to " + ip + ": " + e.getMessage());
                reconnect(socket, ip);
            }
        };
        executor.scheduleAtFixedRate(heartbeatTask, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Reconnects to the button if the connection is lost.
     */
    private void reconnect(Socket socket, String ip) {
        executor.submit(() -> {
            boolean connected = false;
            while (!connected) {
                try {
                    socket.connect(new InetSocketAddress(ip, port), CONNECTION_TIMEOUT);
                    if (socket.isConnected()) {
                        System.out.println("Reconnected to " + ip);
                        connected = true;
                    }
                } catch (IOException e) {
                    System.err.println("Failed to reconnect to " + ip + ": " + e.getMessage());
                    try {
                        Thread.sleep(RECONNECT_DELAY);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    /**
     * Stores received data for later use.
     */
    private void storeReceivedData(String ip, String data) {
        // TODO: Implement logic for storing received messages
        // You could store the data in a database or an in-memory structure
        System.out.println("Storing data from " + ip + ": " + data);
    }

    /**
     * Closes the I/O streams if they are open.
     */
    private void closeResources(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    /**
     * Returns the map of socket instances.
     */
    public Map<String, Socket> getConnections() {
        return connections;
    }
}
