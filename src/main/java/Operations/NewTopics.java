package Operations;

import Application.Topic;

public class NewTopics {
    private String username;
    private Topic topic;

    public NewTopics(String username, Topic topic) {
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