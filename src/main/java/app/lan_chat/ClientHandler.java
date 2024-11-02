package app.lan_chat;

import app.lan_chat.model.ExitMessage;
import app.lan_chat.model.Message;
import app.lan_chat.model.TextMessage;

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

    public ClientHandler(Socket clientSocket, ChatRoomServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // welcome message
            sendMessage(new TextMessage("admin", "Welcome to the chat!"));

            String jsonMessage;
            while ((jsonMessage = in.readLine()) != null) {
                Message message = MessageUtils.deserializeMessage(jsonMessage);
                server.broadcast(message);

                if (message instanceof ExitMessage) {
                    shutdown();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client on port " + server.getPort() + ": " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void sendMessage(Message message) {
        out.println(MessageUtils.serializeMessage(message));
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
}
