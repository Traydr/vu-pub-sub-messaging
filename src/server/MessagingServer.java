package server;

import java.io.IOException;
import java.nio.channels.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicBoolean;

import shared.models.data.Post;
import shared.models.data.Topic;
import shared.models.data.User;
import shared.models.data.Credentials;

import static shared.util.Styling.*;

import server.handlers.ClientHandler;
import server.handlers.TransmissionHandler;

import static server.util.Templates.*;


public class MessagingServer {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isListening = new AtomicBoolean(false);
    private final Selector selector;

    private Thread binder = null;
    private ArrayList<ServerSocketChannel> channels = null;
    private ArrayList<ClientHandler> clients = null;
    private final ArrayList<User> users = new ArrayList<>();
    private final ArrayList<Topic> topics = new ArrayList<>();

    public MessagingServer() throws IOException {
        selector = Selector.open();
    }

    public Post addMessage(String topic, String body, User author) {
        var match = topics.stream().filter(t -> t.getTitle().equals(topic)).findFirst();
        if (match.isPresent()) {
            var post = new Post(match.get(), body, author);
            match.get().addMessage(post);
            return post;
        } else {
            var t = new Topic(topic);
            topics.add(t);
            var post = new Post(t, body, author);
            t.addMessage(post);
            return post;
        }
    }

    public ArrayList<ClientHandler> getClients() {
        return clients;
    }

    public Topic subscribeUser(String topic, User user) {
        var match = topics.stream().filter(t -> t.getTitle().equals(topic)).findFirst();
        if (match.isPresent()) {
            var t = match.get();
            t.addSubscriber(user);
            user.addSubscription(t);
            return t;
        } else return null;
    }

    public User loginUser(Credentials info) {
        synchronized (users) {
            return users
              .stream()
              .filter(u ->
                Objects.equals(u.username(), info.getUsername()) &&
                Objects.equals(u.password(), info.getPasswordHash())
              )
              .findFirst()
              .orElse(null);
        }
    }

    public User registerUser(Credentials info) {
        synchronized (users) {
            var taken = users.stream().anyMatch(u -> u.username().equals(info.getUsername()));
            var user = taken ? null : new User(info);
            if (!taken)
                users.add(user);
            return user;
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

    public int connections() {
        synchronized (clients) {
            if (clients == null) return 0;
            return clients.size();
        }
    }

    public boolean listening() {
        return isListening.get();
    }

    private synchronized void interruptBinder() {
        if (binder != null) {
            binder.interrupt();
            binder = null;
        }
    }

    public synchronized void bind(int[] ports) throws BindException {
        if(!isRunning.get()) return;
        unbind();
        byte i = 0;
        try {
            for (; i < ports.length; i++) {
                var channel = ServerSocketChannel.open();
                channels.add(channel);
                channel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), ports[i]));
                channel.configureBlocking(false).register(selector, SelectionKey.OP_ACCEPT);
            }
        } catch (IOException e) {
            for (var channel : channels)
                try {
                    channel.close();
                } catch (Exception ignored) {}
            throw new BindException(String.format(error__port_range.val, ports[i]));
        }
        String host = "localhost";
        try {
            host = InetAddress.getLocalHost().toString();
        }  catch (UnknownHostException ignored) {}
        printSeparator();
        printMessage(
          String.format(message__network_info.val, host),
          message__ports_info.val + channels
              .stream()
              .map(channel -> String.format(port_format.val, channel.socket().getLocalPort()))
              .collect(Collectors.joining())
              .replaceFirst(", $", ".")
        );
        printSeparator();
        isListening.set(true);
        new Thread(new TransmissionHandler(selector, this)).start();
    }

    public void bind(byte portsAmount) throws BindException {
        bind(new int[portsAmount == 0 ? Config.DEFAULT_PORTS_AMOUNT : portsAmount]);
    }

    public synchronized void unbind() {
        isListening.set(false);
        interruptBinder();
        if (clients != null)
            for (var client : clients)
                client.close();
        clients = new ArrayList<>();
        if (channels != null)
            for (var channel : channels)
                try {
                    channel.close();
                } catch (Exception e) {
                    int port = channel.socket().getLocalPort();
                    printError(error__unbind.val + " " + port);
                }
        channels = new ArrayList<>();
        printSeparator();
        printMessage(message__channels_cleared.val);
        printSeparator();
    }

    public void start() {
        if(isRunning.getAndSet(true)) return;
        synchronized (this) {
            printSeparator();
            printMessage(message__starting.data);
            printSeparator();
            printText(program_title.data);
            printSeparator();
            binder = new Thread(() -> {
                try {
                    requestAndHandleInput(
                      opt -> {
                          switch (opt) {
                              case "auto" -> {
                                  var portsAmount = requestInput(
                                    input -> {
                                        byte amount = Byte.parseByte(input);
                                        if (amount < 0 || amount > 8)
                                            throw new IllegalArgumentException(error__ports_amount.val);
                                        return amount;
                                    }, error__ports_amount.val, request__ports_amount.data
                                  );
                                  bind(portsAmount);
                              }
                              case "manual" -> {
                                  var ports = requestInput(
                                    input -> Arrays.stream(input.split(",")).mapToInt(
                                      val -> {
                                          int port = Integer.parseUnsignedInt(val.trim());
                                          if (port > 65345)
                                              throw new IllegalArgumentException(error__port_range.val);
                                          return port;
                                      }
                                    ).toArray(), error__port_range.val, request__ports.data
                                  );
                                  bind(ports);
                              }
                              default -> throw new IllegalArgumentException(error__binding.val);
                          }
                      }, "", request__binding.data
                    );
                } catch (InterruptedException ignored) {}
            });
            binder.start();
        }
        try {
            binder.join();
        } catch (InterruptedException ignored) {
        } finally {
            interruptBinder();
        }
    }

    public synchronized void stop() {
        if (!isRunning.getAndSet(false)) return;
        unbind();
        printSeparator();
        printMessage(message__stopping.data);
        printSeparator();
    }

    public static void main(String[] args) {
        try {
            var server = new MessagingServer();
            requestAndHandleInput(
              input -> {
                  switch (input) {
                      case "exit" -> {}
                      case "run" -> {
                          server.start();
                          printSeparator();
                          printMessage(message__enter_to_stop.data);
                          printSeparator();
                          blockUntilEnter();
                          server.stop();
                          throw new RuntimeException("");
                      }
                      default -> throw new IllegalArgumentException(error__startup.val);
                  }
              }, "", request__startup.data
            );
        } catch (Exception ignored) {}
    }

}