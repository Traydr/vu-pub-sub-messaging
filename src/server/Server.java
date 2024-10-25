package server;

import java.net.BindException;
import java.util.Scanner;

public class Server {
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
