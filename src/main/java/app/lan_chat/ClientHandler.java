package app.lan_chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatRoomServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String nickname;

    public ClientHandler(Socket clientSocket, ChatRoomServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out.println("Please enter your nickname:");
            nickname = in.readLine();
            System.out.println(nickname + " has joined the chat");

            server.broadcast(nickname + " has joined the chat!");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("/exit")) {
                    server.broadcast(nickname + " has left the chat.");
                    shutdown();
                    break;
                } else {
                    server.broadcast(nickname + ": " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client on port " + server.getPort() + ": " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void shutdown() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            server.removeClient(this);
        } catch (IOException e) {
            System.err.println("Error shutting down client handler: " + e.getMessage());
        }
    }

    public String getNickname() {
        return nickname;
    }
}
