package server.handlers;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;

import shared.protocols.TCP;

import static shared.util.Styling.*;

import server.Config;
import server.MessagingServer;

import static server.util.Templates.*;


public class TransmissionHandler implements TCP, Runnable {

    private final Selector selector;
    private final MessagingServer server;

    public TransmissionHandler(Selector selector, MessagingServer instance) {
        this.selector = selector;
        this.server = instance;
    }

    @Override
    public void run() {
        while (server.listening()) {
            try {
                if (selector.select(Config.SELECTOR_TIMEOUT_MILLIS) == 0) {
                    if (!server.listening()) return;
                    tickLoader(
                      "Waiting for " + (server.connections() > 0 ? "new connections or events" : "connection requests")
                    );
                    continue;
                }
            } catch (IOException e) {
                printError(error__transmission.val);
                server.stop();
                return;
            }
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while (keyIter.hasNext()) {
                if (!server.listening()) return;
                var key = keyIter.next();
                if (key.isAcceptable())
                    handleAccept(key);
                else {
                    if (key.isReadable())
                        handleRead(key);
                    if (key.isValid() && key.isWritable())
                        handleWrite(key);
                }
                keyIter.remove();
            }
        }
    }

    @Override
    public void handleAccept(SelectionKey key) {
        try {
            var serverChannel = (ServerSocketChannel) key.channel();
            var channel = serverChannel.accept();
            var client = new ClientHandler(
              channel.socket().getInetAddress(),
              channel.socket().getPort(),
              serverChannel.socket().getLocalPort(),
              channel, server
            );
            channel.configureBlocking(false).register(
              key.selector(), SelectionKey.OP_READ, client
            );
            printMessage(
              String.format(
                message__new_connection.val,
                client.localPort, client.address, client.foreignPort
              )
            );
            server.addClient(client);
        } catch (IOException e) {
            printMessage(message__connection_unsuccessful.data);
        }
    }

    @Override
    public void handleRead(SelectionKey key) {
        var client = (ClientHandler) key.attachment();
        try {
            if(client.receive())
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            return;
        } catch (ClosedChannelException e) {
            printMessage(
              String.format(
                message__connection_terminated.val,
                client.localPort, client.address, client.foreignPort
              )
            );
            server.removeClient(client);
        } catch (IOException e) {
            printMessage(
              String.format(
                message__connection_dropped.val,
                client.localPort, client.address, client.foreignPort
              )
            );
            server.removeClient(client);
        }
        client.close();
        key.cancel();
    }

    @Override
    public void handleWrite(SelectionKey key) {
        var client = (ClientHandler) key.attachment();
        try {
            if (!client.send())
                key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            printMessage(
              String.format(
                message__connection_dropped.val,
                client.localPort, client.address, client.foreignPort
              )
            );
            client.close();
            key.cancel();
        }
    }

}