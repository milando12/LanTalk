package app.lan_chat;

import app.lan_chat.model.ChatRoom;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            List<ChatRoom> rooms = fetchAvailableRooms();
            displayAvailableRooms(rooms);

            ChatRoom selectedRoom = promptRoomSelection(rooms);
            if (selectedRoom == null) return;

            // Connect to selected room
            socket = new Socket("127.0.0.1", selectedRoom.getPort());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start input handling thread
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            // Read and print incoming messages
            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
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
            System.out.println("No available rooms. You can create a new one.");
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
        int port = scanner.nextInt();   // TODO: handle invalid input
        scanner.nextLine();

        // there can be a case where room does not exist in rooms
        // but there is a room on the port

        for (ChatRoom room : rooms) {
            if (room.getPort() == port) {
                return room;
            }
        }

        // port is not found in existing rooms => create a new room
        System.out.print("Enter a name for the new room: ");
        String roomName = scanner.nextLine();
        return createNewRoom(port, roomName);
    }

    /**
     * Create a new chat room on the control server
     * @param port Port number for the new room
     * @param name Name of the new room
     * @return New ChatRoom object
     */
    private ChatRoom createNewRoom(int port, String name) {
        ChatRoom newRoom = new ChatRoom(name, port);

        try {
            URL url = new URL("http://localhost:8080/control/new-room");
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
                // someone else created the room on the same port before us
                System.out.println("Failed to create a new room.");
                conn.disconnect();
                run();  // Restart the client
                return null;
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Error creating new room: " + e.getMessage());
        }
        return newRoom;
    }

    /**
     * Shutdown the client gracefully and close all resources
     */
    public void shutdown() {
        done = true;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error shutting down: " + e.getMessage());
        }
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try (Scanner scanner = new Scanner(System.in)) {
                while (!done) {
                    String message = scanner.nextLine();
                    if (message.equalsIgnoreCase("/exit")) {
                        out.println(message);   // tell the server that we are disconnecting
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }
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
