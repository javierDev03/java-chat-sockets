package com.chat.server;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import com.chat.database.DatabaseConnection;

public class ChatServer {

    private List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(writer);
                new Thread(new ClientHandler(clientSocket, writer)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;

        public ClientHandler(Socket socket, PrintWriter writer) {
            this.socket = socket;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    broadcastMessage(message);
                    saveMessageToDatabase(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void saveMessageToDatabase(String message) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String[] splitMessage = message.split(": ", 2);
                String name = splitMessage[0];
                String chatMessage = splitMessage[1];

                String query = "INSERT INTO mensajes (nombre, mensaje) VALUES (?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, name);
                stmt.setString(2, chatMessage);
                stmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
