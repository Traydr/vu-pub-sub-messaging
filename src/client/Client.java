package client;

import java.util.Scanner;

public class Client {
    private String host;
    private int port;
    private ClientSocket socket;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.socket = new ClientSocket();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Client client;

        System.out.println("Please enter server host");
        String host = scanner.nextLine();
        System.out.println("Please enter server port");
        int port = Integer.parseInt(scanner.nextLine());

        client = new Client(host, port);

        try {
            if (!client.socket.connect(host, port)) {
                System.out.println("Couldn't connect to server");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String input;
        while (scanner.hasNextLine() && client.socket != null) {
            input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            }

            client.socket.send(input);
        }

        if (client.socket != null) {
            client.socket.close();
        }
        scanner.close();
        System.out.println("Quiting");
    }
}
