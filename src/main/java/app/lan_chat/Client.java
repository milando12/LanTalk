package app.lan_chat;

import app.lan_chat.model.ChatRoom;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        List<ChatRoom> rooms = fetchAvailableRooms();
        displayAvailableRooms(rooms);

        ChatRoom selectedRoom = promptRoomSelection(rooms);
    }

    /**
     * Fetch available chat rooms from the control server
     * @return List of available chat rooms
     */
    private List<ChatRoom> fetchAvailableRooms() {
        List<ChatRoom> rooms = new ArrayList<>();
        try {
            URL url = new URL("http://localhost:8080/control/rooms");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String response = reader.readLine();
                    rooms = parseRooms(response); // Parse JSON response into ChatRoom objects
                }
            } else {
                System.out.println("Failed to retrieve rooms: " + conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Error fetching available rooms: " + e.getMessage());
        }
        return rooms;
    }

    /**
     * Display available chat rooms to the user
     * @param rooms List of available chat rooms
     */
    private void displayAvailableRooms(List<ChatRoom> rooms) {
        System.out.println("Available Chat Rooms:");
        if (rooms.isEmpty()) {
            System.out.println("No available rooms.");
        } else {
            for (ChatRoom room : rooms) {
                System.out.println("Room: " + room.getName() + " - Port: " + room.getPort());
            }
        }
    }

    /**
     * Prompt the user to select an existing chat room or create a new one
     * @param rooms List of available chat rooms
     * @return Selected chat room
     */
    private ChatRoom promptRoomSelection(List<ChatRoom> rooms) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a port to join an existing room, or enter a new port to create a room: ");
        int port = scanner.nextInt();
        scanner.nextLine();

        for (ChatRoom room : rooms) {
            if (room.getPort() == port) {
                return room;
            }
        }

        // port not found in existing rooms => create a new room
        System.out.print("Enter a name for the new room: ");
        String roomName = scanner.nextLine();
        return createNewRoom(port, roomName);
    }

    // Send a POST request to create a new chat room
    private ChatRoom createNewRoom(int port, String name) {
        ChatRoom newRoom = new ChatRoom(name, port);
        try {
            URL url = new URL("http://localhost:8080/control/rooms");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send room details in JSON format
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = String.format("{\"port\": %d, \"name\": \"%s\"}",
                        port, name).getBytes();
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                System.out.println("New chat room created successfully on port " + port);
            } else {
                System.out.println("Failed to create room: " + conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Error creating new room: " + e.getMessage());
        }
        return newRoom;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    // Parsing JSON response into a list of ChatRoom objects
    private List<ChatRoom> parseRooms(String jsonResponse) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, new TypeToken<List<ChatRoom>>() {}.getType());
    }
}
