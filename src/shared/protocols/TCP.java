package shared.protocols;

import java.nio.channels.SelectionKey;


public interface TCP {

    void handleAccept(SelectionKey key);
    void handleRead(SelectionKey key);
    void handleWrite(SelectionKey key);

}