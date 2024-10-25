package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListener implements Runnable {
    private final Socket socket;
    private BufferedReader reader;

    public ClientListener(Socket socket) {
        this.socket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Socket was not able to be opened");
        }
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Reader already closed");
        }
    }

    public void parse(String message) {
        System.out.println(message);
    }

    @Override
    public void run() {
        if (this.socket.isClosed()) {
            return;
        }

        try {
            String input;
            while ((input = reader.readLine()) != null) {
                parse(input);
            }
        } catch (IOException e) {
            System.out.println("Socket unexpectedly closed");
        }
    }
}
