package shared.models.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


public class Post implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private final Topic topic;
    private String body;
    private final Date createdAt;
    private final User author;

    public Post(Topic topic, String body, User author) { //, ArrayList<User> viewedBy
        this.topic = topic;
        this.body = body;
        this.createdAt = new Date();
        this.author = author;
    }

    public Topic topic() {
        return topic;
    }

    public String body() {
        return body;
    }

    public Date createdAt() {
        return createdAt;
    }

    public User author() {
        return author;
    }

}