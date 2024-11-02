package app.lan_chat;

import app.lan_chat.model.Message;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatRoomServer implements Runnable {
    private final int port;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients;
    private boolean running;
    private final ExecutorService pool;

    public ChatRoomServer(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.running = true;
        this.pool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Chat room server started on port: " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Error starting chat room server on port " + port + ": " + e.getMessage());
            shutdown();
        }
    }

    public void broadcast(Message message) {
        for (ClientHandler client : clients) {
            if (client != null ) {          // TODO: do not return the message to the client who has sent it
                client.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.shutdown();
            }
            pool.shutdown();
        } catch (IOException e) {
            System.err.println("Error shutting down chat room server on port " + port + ": " + e.getMessage());
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public int getPort() {
        return port;
    }
}
