package shared.models.data;

import java.util.ArrayList;


public class User {

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