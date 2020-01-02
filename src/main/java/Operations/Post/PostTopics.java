package Operations.Post;

import java.util.List;

import Application.Topic;

public class PostTopics {
    private String username;
    private List<Topic> topics;

    public PostTopics(String username, List<Topic> topic) {
        this.username = username;
        this.topics = topic;
    }

    public String getUsername() {
        return username;
    }

    public List<Topic> getTopics() {
        return topics;
    }

}