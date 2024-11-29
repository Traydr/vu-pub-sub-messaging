package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AcceptConnection implements Runnable{
    ServerSocket serverSocket;
    MessagingServer messagingServer;

    public AcceptConnection(ServerSocket serverSocket, MessagingServer messagingServer) {
        this.serverSocket = serverSocket;
        this.messagingServer = messagingServer;
    }

    @Override
    public void run() {
        Socket client = null;
        while (!serverSocket.isClosed()) {
            try {
                client = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("Socket is closed");;
            }

            if (client != null) {
                ClientHandler clientHandler = null;
                try {
                    clientHandler = new ClientHandler(client, messagingServer);
                } catch (IOException e) {
                    System.out.println("Socket is closed");
                }
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
    }
}
