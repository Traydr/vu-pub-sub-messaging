package server.handlers;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import shared.models.communication.Response;
import shared.models.communication.ResponseType;
import shared.models.data.User;
import shared.models.data.Credentials;
import shared.models.communication.Request;
import shared.models.communication.TransmissionBuffer;

import static shared.util.Styling.*;
import static shared.util.Styling.Colors.*;

import server.MessagingServer;
import shared.models.generics.Pair;
import shared.util.Styling;


public class ClientHandler {

    public final InetAddress address;
    public final int foreignPort;
    public final int localPort;

    private TransmissionBuffer buffer;
    private SocketChannel channel;
    private MessagingServer server;
    private User user = null;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public ClientHandler(
      InetAddress address, int foreignPort, int localPort, SocketChannel channel, MessagingServer server
    ) {
        this.address = address;
        this.foreignPort = foreignPort;
        this.localPort = localPort;
        this.channel = channel;
        this.server = server;
        buffer = TransmissionBuffer.allocate(-1);
    }

    public synchronized boolean receive() throws IOException {
        if (closed.get()) return false;
        var success = buffer.read(channel);
        if (success)
            for (var obj : buffer.retrieveObjects()) {
                if (obj instanceof Request request) {
                    switch (request.getType()) {
                        case Ping -> buffer.storeObject(new Response(ResponseType.Echo, null));
                        case Disconnect -> throw new ClosedChannelException();
                        case Login -> {
                            if (user != null) {
                                buffer.storeObject(
                                  new Response(ResponseType.InvalidCommand, "You are already logged in.")
                                );
                                return success;
                            }
                            user = server.loginUser((Credentials) request.getPayload());
                            if (user != null)
                                printMessage(
                                  String.format(
                                    GRAY + '[' + YELLOW + "%d" + GRAY + ']' + RESET + ": " +
                                      PURPLE + "Client " + BLUE + "%s" + BLACK_BRIGHT + ':' + BLUE + "%d" +
                                      PURPLE + " signed in as " + BLUE + "%s" + GRAY + '.',
                                    localPort, address, foreignPort, user.username()
                                  )
                                );
                            buffer.storeObject(user == null
                                ? new Response(
                                    ResponseType.InvalidCredentials,
                                    "Login failed, invalid username or password."
                                )
                                : new Response(
                                    ResponseType.AuthorizationSuccess,
                                    new Pair<>(
                                      "Login successful!",
                                      user.username()
                                    )
                                )
                            );
                        }
                        case Register -> {
                            if (user != null) {
                                buffer.storeObject(
                                  new Response(ResponseType.InvalidCommand, "You are already logged in.")
                                );
                                return success;
                            }
                            user = server.registerUser((Credentials) request.getPayload());
                            if (user != null)
                                printMessage(
                                  String.format(
                                    GRAY + '[' + YELLOW + "%d" + GRAY + ']' + RESET + ": " +
                                      PURPLE + "Client " + BLUE + "%s" + BLACK_BRIGHT + ':' + BLUE + "%d" +
                                      PURPLE + " registered as " + BLUE + "%s" + GRAY + '.',
                                    localPort, address, foreignPort, user.username()
                                  )
                                );
                            buffer.storeObject(user == null
                              ? new Response(
                                ResponseType.UsernameTaken,
                                "Registration failed, username already exists."
                              )
                              : new Response(
                                ResponseType.RegistrationSuccess,
                                new Pair<>(
                                  "Registration successful! You can now log in.",
                                  user.username()
                                )
                              )
                            );
                        }
                        case Publish -> {
                            if (user == null) {
                                buffer.storeObject(new Response(ResponseType.AccessDenied, "You are not logged in."));
                                return success;
                            }
                            var data = (Pair<String, String>) request.getPayload();
                            var message = server.addMessage(data.getFirst(), data.getSecond(), user);
                            if (message != null) {
                                for (var c : server.getClients())
                                    if (c.user != null && c.user.getSubscriptions().contains(message.topic()))
                                        c.buffer.storeObject(new Response(ResponseType.NewPublication, message));
                                printMessage(
                                  String.format(
                                    GRAY + '[' + YELLOW + "%d" + GRAY + ']' + RESET + ": " + PURPLE + "Client " +
                                      BLUE + "%s" + PURPLE + " published a message in " + BLUE + "%s" + GRAY + '.',
                                    localPort, user.username(), message.topic().getTitle()
                                  )
                                );
                            }
                            buffer.storeObject(message == null
                              ? new Response(
                                ResponseType.InvalidCommand,
                                "Title may not contain ':', both title and body cannot be empty."
                              )
                              : new Response(
                                ResponseType.PublishedMessage,
                                "You message has been published."
                              )
                            );
                        }
                        case Subscribe -> {
                            if (user == null) {
                                buffer.storeObject(new Response(ResponseType.AccessDenied, "You are not logged in."));
                                return success;
                            }
                            var title = (String) request.getPayload();
                            var topic = server.subscribeUser(title, user);
                            if (topic != null)
                                printMessage(
                                  String.format(
                                    GRAY + '[' + YELLOW + "%d" + GRAY + ']' + RESET + ": " + PURPLE + "Client " +
                                      BLUE + "%s" + PURPLE + " subscribed to topic " + BLUE + "%s" + GRAY + '.',
                                    localPort, user.username(), topic.getTitle()
                                  )
                                );
                            buffer.storeObject(topic == null
                              ? new Response(ResponseType.UnknownTopic, "Unknown topic.")
                              : new Response(
                                ResponseType.TopicSubscribed, "You have successfully subscribed to topic."
                              )
                            );
                        }
                    }
                } else buffer.storeObject(new Response(ResponseType.InvalidCommand, null));
            }
        return success;
    }

    public synchronized boolean send() throws IOException {
        return !closed.get() && buffer.write(channel);
    }

    public synchronized void close() {
        if (closed.get()) return;
        try {
            channel.close();
        } catch (IOException ignored) {}
        server.removeClient(this);
        buffer = null;
        channel = null;
        server = null;
        closed.set(true);
    }

}