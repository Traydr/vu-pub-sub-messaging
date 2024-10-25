package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket client;
    private final MessagingServer server;
    private final BufferedWriter writer;
    private boolean isClosed;

    public ClientHandler(Socket client, MessagingServer server) throws IOException {
        this.client = client;
        this.server = server;
        this.writer = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
        this.isClosed = false;
    }

    public void close() {
        if (isClosed) {
            return;
        }

        try {
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            System.out.println("Socket is already closed");
        } finally {
            server.removeClient(this);
            isClosed = true;
        }
    }

    public void send(String message) {
        try {
            if (!this.client.isClosed()) {
                this.writer.write(message + "\n");
                this.writer.flush();
                return;
            }
            close();
        } catch (IOException e) {
            System.out.println();
            close();
        }
    }

    public void parse(String message) {
        System.out.println("Message from client: " + message);
        if (message.equals("exit")) {
            close();
        }
    }

    @Override
    public void run() {
        server.addClient(this);
        try (Scanner scanner = new Scanner(new InputStreamReader(this.client.getInputStream()))) {
            String input;
            while (scanner.hasNextLine()) {
                parse(scanner.nextLine());
            }
        } catch (IOException e) {
            System.out.println("Socket is already closed");
        } finally {
            close();
        }
    }
}
