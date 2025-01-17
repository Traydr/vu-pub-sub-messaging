package shared.models.data;

import server.handlers.ClientHandler;

import java.util.ArrayList;


public class Topic {

    private String title;
    private ArrayList<Post> messages;
    private ArrayList<User> subscribers;

    public Topic(String title) {
        this.title = title;
        this.messages = new ArrayList<>();
        this.subscribers = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void addMessage(Post post) {
        messages.add(post);
    }

    public void addSubscriber(User user) {
        subscribers.add(user);
    }

    public ArrayList<User> getSubscribers() {
        return subscribers;
    }

    //public static void getUnseenMessages() {}
    //package shared.models.data;
    //
    //import java.util.ArrayList;
    //
    //public class Subscription {
    //
    //    private Topic topic;
    //    private ArrayList<Post> unreadMessages;
    //
    //}
}