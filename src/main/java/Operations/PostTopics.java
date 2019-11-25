package Operations;

import Application.Topic;

public class PostTopics {
    private String username;
    private Topic topic;

    public PostTopics(String username, Topic topic) {
        this.username = username;
        this.topic = topic;
    }

    public String getUsername() {
        return username;
    }

    public Topic getTopic() {
        return topic;
    }

}