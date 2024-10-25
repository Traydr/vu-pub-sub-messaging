package client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class ClientSocket {
    private Socket socket;
    private BufferedWriter writer;
    private ClientListener listener;

    public boolean connect(String host, int port) {

        try {
            this.socket = new Socket(host, port);
            this.writer = new BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
            this.listener = new ClientListener(socket);
            Thread thread = new Thread(listener);
            thread.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void close() {
        try {
            this.writer.close();
            this.listener.close();
            this.socket.close();
        } catch (IOException e) {
            System.out.println("At Least 1 Socket was already closed");
        }
    }

    public void send(String message) {
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {
            System.out.println("Server is not responding");
            close();
        }
    }
}
