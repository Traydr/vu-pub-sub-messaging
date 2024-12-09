package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private final Socket client;
    private final MessagingServer server;
    private final BufferedWriter writer;
    private boolean isClosed;
    private boolean isAuthenticated;
    private boolean hasIdentified;
    private boolean isPublisher;
    private final Set<String> topics;

    public ClientHandler(Socket client, MessagingServer server) throws IOException {
        this.client = client;
        this.server = server;
        this.writer = new BufferedWriter(new OutputStreamWriter(this.client.getOutputStream()));
        this.isClosed = false;
        this.isAuthenticated = false;
        this.hasIdentified = false;
        this.isPublisher = false;
        this.topics = new HashSet<>();
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
            close();
        }
    }

    public void sendForTopic(String topic, String message) {
        if (isPublisher) return;
        if (!topics.contains(topic)) return;
        send(topic + ":" + message);
    }

    public void parse(String message) {
        String[] parts = message.split(" ");

        if (parts.length == 0) {
            send("Invalid request, message must not be empty");
            return;
        }

        if (!isAuthenticated) {
            switch (parts[0]) {
                case "register" -> {
                    if (parts.length != 3) {
                        send("Invalid request, message must be \"register [username] [password]\"");
                        return;
                    }
                    boolean success = server.registerUser(parts[1], parts[2]);
                    if (success) {
                        send("Registration successful! You can now log in.");
                    } else {
                        send("Registration failed, username already exists.");
                    }
                }
                case "login" -> {
                    if (parts.length != 3) {
                        send("Invalid request, message must be \"login [username] [password]\"");
                        return;
                    }
                    boolean success = server.authenticateUser(parts[1], parts[2]);
                    if (success) {
                        send("Login successful!");
                        isAuthenticated = true;
                    } else {
                        send("Login failed, invalid username or password.");
                    }
                }
                default -> send("Invalid request, you must register or log in first.");
            }
            return;
        }

        if (!hasIdentified) {
            if (parts[0].equals("pub")) {
                isPublisher = true;
                send("Identified as publisher");
            } else if (parts[0].equals("sub")) {
                if (parts.length != 2) {
                    send("Invalid request, message must be \"sub [topic]\"");
                    return;
                }
                isPublisher = false;
                topics.add(parts[1]);
                send("Identified as subscribed for topic " + parts[1]);
            } else {
                send("Invalid request, must first identify as publisher or subscriber");
                return;
            }

            hasIdentified = true;
            return;
        }

        switch (parts[0]) {
            case "disconnect" -> close();
            case "sub" -> {
                if (isPublisher) {
                    send("Invalid request, cannot subscribe to a topic while being a publisher");
                    return;
                } else if (parts.length != 2) {
                    send("Invalid request, when subscribing the total length of command must be 2");
                    return;
                } else if (!isValidTopic(parts[1])) {
                    send("Invalid request, topic name contains invalid characters");
                }

                topics.add(parts[1]);
            }
            case "pub" -> {
                if (!isPublisher) {
                    send("Invalid request, cannot publish to a topic while being a subscriber");
                    return;
                } else if (parts.length < 3) {
                    send("Invalid request, when publishing the total length of command must be at least 3");
                    return;
                } else if (!isValidTopic(parts[1])) {
                    send("Invalid request, topic name contains invalid characters");
                }

                String topic = parts[1];
                String topicMessage = Arrays.stream(parts).skip(2).collect(Collectors.joining());

                server.sendToTopic(topic, topicMessage);
            }
            default -> send("Invalid request, command was not recognized");
        }
    }

    private boolean isValidTopic(String topic) {
        return !topic.contains(":");
    }

    @Override
    public void run() {
        server.addClient(this);
        try (Scanner scanner = new Scanner(new InputStreamReader(this.client.getInputStream()))) {
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
