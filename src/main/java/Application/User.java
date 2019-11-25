package Application;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String username;
    private String password;
    private Map<Topic, Long> topics;

    // CONSTRUCTOR

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.topics = new HashMap<>();
    }

    // GETTERS & SETTERS

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<Topic, Long> getTopics() {
        return topics;
    }

    public void setTopics(Map<Topic, Long> topics) {
        this.topics = topics;
    }

}