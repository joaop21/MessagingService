package Application;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    public synchronized Map<Topic, Long> getTopics() {
        return topics;
    }

    public synchronized void setTopics(Map<Topic, Long> topics) {
        this.topics = topics;
    }

    public synchronized  void setTopics(List<Topic> tps){
        long subscription = new Date().getTime();
        for (Topic t : tps){
            if (this.topics.containsKey(t)){
                this.topics.replace(t, subscription);
            }
            else this.topics.put(t, subscription);
        }
    }

}