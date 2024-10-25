package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class MessagingServer {
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients;

    public void start(int port) throws BindException {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening at: " + serverSocket.getLocalSocketAddress());
            this.clients = new ArrayList<>();

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

    public int getPort() {
        return serverSocket.getLocalPort();
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
}
