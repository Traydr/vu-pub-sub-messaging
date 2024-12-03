package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;

public class MessagingServer {
    private ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clients;

    public MessagingServer() {
        this.clients = new ArrayList<>();
    }

    public void start(int port) throws BindException {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening at: " + serverSocket.getLocalSocketAddress());

            Thread acceptConnection = new Thread(new AcceptConnection(serverSocket, this));
            acceptConnection.start();
        } catch (IOException e) {
            System.out.println("Couldn't bind to this port, please enter another one");
        }
    }

    public void addClient(ClientHandler client) {
        synchronized (clients) {
            clients.add(client);
        }
    }

    public void removeClient(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
        }
    }

    public void sendToTopic(String topic, String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendForTopic(topic, message);
            }
        }
    }

    public void stop() {
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Socket is already closed");
        }
        System.out.println("Server stopped");
    }

    public static void main(String[] args) {
        MessagingServer server = new MessagingServer();
        try (Scanner scanner = new Scanner(System.in)) {
            boolean validPort = false;

            while (!validPort) {
                try {
                    System.out.println("What port should the server listen on (0 for random)?");
                    int port = Integer.parseInt(scanner.nextLine());
                    server.start(port);
                    validPort = true;
                } catch (BindException e) {
                    System.out.println("Couldn't bind to this port, please enter another one");
                }
            }
            String isClosing = "";
            while (!isClosing.equals("quit")) {
                isClosing = scanner.nextLine();
            }
            server.stop();
        }
    }
}
