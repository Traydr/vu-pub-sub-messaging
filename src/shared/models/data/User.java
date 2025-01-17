package shared.models.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;


public class User implements Serializable  {

    @Serial
    private static final long serialVersionUID = 1;

    private final Credentials info;
    private final ArrayList<Topic> subscriptions;

    public User(Credentials info) {
        this.info = info;
        subscriptions = new ArrayList<>();
    }

    public String username() {
        return info.getUsername();
    }

    public String password() {
        return info.getPasswordHash();
    }

    public void addSubscription(Topic topic) {
        if (!subscriptions.contains(topic))
            subscriptions.add(topic);
    }

    public ArrayList<Topic> getSubscriptions() {
        return subscriptions;
    }

}