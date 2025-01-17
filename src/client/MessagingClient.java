package client;

import java.io.IOException;
import java.nio.channels.*;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import shared.models.communication.*;
import shared.models.data.Credentials;
import shared.models.data.Post;
import shared.models.generics.Pair;
import shared.util.Styling;

import static shared.util.Styling.*;
import static shared.util.Styling.Colors.*;

import static client.util.Templates.*;


public class MessagingClient implements Runnable {

    private SocketChannel channel = null;
    private String username = "Guest";
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    public MessagingClient() {}

    public void connect(String address, int port) throws InterruptedException {
        try {
            if (!isRunning.get()) throw new UnsupportedOperationException("Messaging client is not running");
            disconnect();
            isConnecting.set(true);
            byte connectionAttempts = 3;
            for (;;) {
                try {
                    channel = SocketChannel.open();
                    channel.configureBlocking(false);
                    if (channel.connect(new InetSocketAddress(address, port)) || channel.finishConnect()) break;
                } catch (IOException e) {
                    channel.close();
                    channel = null;
                    throw new UnresolvedAddressException();
                }
                try {
                    while (!channel.finishConnect()) {
                        if (!isConnecting.get()) {
                            printMessage(GRAY + "Connection aborted ...");
                            return;
                        }
                        tickLoader("Establishing connection");
                        Thread.sleep(Config.REQUEST_TIMEOUT_MILLIS);
                    }
                    break;
                } catch (IOException ex) {
                    if (--connectionAttempts == 0) {
                        channel.close();
                        channel = null;
                        throw new IOException();
                    }
                    printMessage("Request reached timeout, sending another ...");
                    printSeparator();
                }
            }
            isConnecting.set(false);
            isConnected.set(true);
            printMessage(
              PURPLE + "Traffic is now routed to " + BLUE + address + BLACK_BRIGHT + ':' + BLUE + port + GRAY + '.'
            );
            printSeparator();
            var buffer = TransmissionBuffer.allocate();
            var reading = new Thread(() -> {
                while (true) {
                    try {
                        if (channel == null) break;
                        if (!buffer.read(channel)) {
                            for (var obj : buffer.retrieveObjects()) {
                                var response = (Response) obj;
                                //printError(response.getType().toString());
                                if (response.getType() == ResponseType.NewPublication) {
                                    var post = (Post) response.getPayload();
                                    printMessage(
                                      GRAY + '<' + CYAN + "New post" + GRAY + '>' +
                                        RESET + ": " + BLUE + post.topic().getTitle() + GRAY + ": " +
                                        RESET + post.body()
                                    );
                                } else if (
                                  response.getType() == ResponseType.AuthorizationSuccess ||
                                    response.getType() == ResponseType.RegistrationSuccess
                                ) {
                                    username = ((Pair<String, String>) response.getPayload()).getSecond();
                                    printMessage(
                                      GRAY + '<' + CYAN + "Server" + GRAY + '>' +
                                        RESET + ": " + ((Pair<String, String>) response.getPayload()).getFirst().toString()
                                    );
                                } else {
                                    printMessage(
                                      GRAY + '<' + CYAN + "Server" + GRAY + '>' +
                                        RESET + ": " + response.getPayload().toString()
                                    );
                                }
                            }
                        }
                    } catch (IOException ignored) {}
                }
            });
            try {
                reading.start();
                for (;;) {
                    var request = requestInput(
                      input -> {
                          var commandEnd = input.indexOf(' ');
                          if (commandEnd == -1)
                              commandEnd = input.length();
                          var command = input.substring(0, commandEnd);
                          var arguments = commandEnd != input.length() ? input.substring(commandEnd + 1).trim() : null;
                          return switch (command) {
                              case "disconnect" -> new Request(RequestType.Disconnect);
                              case "login" -> {
                                  String[] parts;
                                  if (arguments == null || (parts = arguments.split("[ \\t]+")).length != 2)
                                      throw new IllegalArgumentException(
                                        "The correct format is \"login [username] [password]\""
                                      );
                                  yield new Request(RequestType.Login, new Credentials(parts[0], parts[1]));
                              }
                              case "register" -> {
                                  String[] parts;
                                  if (arguments == null || (parts = arguments.split("[ \\t]+")).length != 2)
                                      throw new IllegalArgumentException(
                                        "The correct format is \"register [username] [password]\""
                                      );
                                  yield new Request(RequestType.Register, new Credentials(parts[0], parts[1]));
                              }
                              case "sub" -> {
                                  if (arguments == null || (arguments.split("[ \\t]+")).length != 1)
                                      throw new IllegalArgumentException(
                                        "The correct format is \"sub [topic]\""
                                      );
                                  yield new Request(RequestType.Subscribe, arguments);
                              }
                              case "pub" -> {
                                  String body;
                                  if (arguments == null || !arguments.contains(" ") ||
                                    (body = arguments.substring(arguments.indexOf(" ") + 1).trim()).isBlank())
                                      throw new IllegalArgumentException(
                                        "The correct format is \"pub [topic] [message]\""
                                      );
                                  yield new Request(
                                    RequestType.Publish,
                                    new Pair<>(arguments.substring(0, arguments.indexOf(" ")), body)
                                  );
                              }
                              default -> {
                                  Thread.sleep(1000);
                                  throw new IllegalArgumentException("Unknown command");
                              }
                          };
                      }, "", GRAY + '<' + CYAN + username + GRAY + '>'
                    );
                    Thread.sleep(500);
                    buffer.storeObject(request);
                    while (buffer.write(channel));
                    if (request.getType() == RequestType.Disconnect)
                        break;
                    Thread.sleep(500);
                }
            } finally {
                disconnect();
            }
        } catch (UnresolvedAddressException ex) {
            printError("Unknown host, IP address is unreachable");
        } catch (IOException ex) {
            printText(RED + "Server socket is unreachable.");
        } catch (UnsupportedOperationException ignored) {}
    }

    public void disconnect() {
        isConnecting.set(false);
        try {
            if (channel != null)
                channel.close();
            channel = null;
        } catch (IOException ignored) {}
        if(isConnected.getAndSet(false)) {
            username = "Guest";
            printSeparator();
            printMessage(
              GRAY + "Client socket closed,",
              GRAY + "disconnecting from the server ... "
            );
        }
    }

    public boolean running() {
        return isRunning.get();
    }

    public boolean connected() {
        return isConnected.get();
    }

    public static void main(String[] args) {
        var client = new MessagingClient();
        try {
            requestAndHandleInput(
              input -> {
                  switch (input) {
                      case "exit" -> {}
                      case "run" -> {
                          client.run();
                          throw new RuntimeException();
                      }
                      default -> throw new IllegalArgumentException(error__startup.val);
                  }
              }, "", request__startup.data
            );
        } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        synchronized(this) {
            if(isRunning.getAndSet(true)) return;
            printSeparator();
            printMessage(message__starting.data);
            printSeparator();
            printText(program_title.data);
            printSeparator();
        }
        try {
            requestAndHandleInput(
              opt -> {
                  switch (opt) {
                      case "stop" -> {}
                      case "join" -> {
                          var ip = requestInput(request__address.data);
                          var port = requestInput(
                            input -> {
                                var val = Integer.parseUnsignedInt(input.trim());
                                if (val > 65345)
                                    throw new IllegalArgumentException(error__port_range.val);
                                return val;
                            }, error__port_range.val, request__port.data
                          );
                          printSeparator();
                          connect(ip, port);
                          throw new RuntimeException();
                      }
                      default -> throw new IllegalArgumentException(error__join.val);
                  }
              }, "", request__join.data
            );
        } catch (InterruptedException ignored) {}
        synchronized (this) {
            printSeparator();
            printMessage(message__stopping.data);
            printSeparator();
            isRunning.set(false);
        }
    }

}