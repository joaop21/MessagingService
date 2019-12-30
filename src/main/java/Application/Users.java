package Application;

import java.util.*;

public class Users {
    private Map<String, User> users;

    public Users(Map<String, User> users){
        this.users = users;
    }

    public Users(){
        this.users = new HashMap<>();
        this.users.put("joao", new User("joao","joaopass"));
        this.users.put("henrique", new User("henrique","henriquepass"));
    }

    public boolean contains(String username){
        return this.users.containsKey(username);
    }

    public boolean is_auth(String username, String password){
        return this.users.containsKey(username) && this.users.get(username).is_auth(password);
    }

    public boolean set_topics(String username, List<Topic> topics) {
        if(this.users.containsKey(username)){
            this.users.get(username).setTopics(topics);
            return true;
        }
        return false;
    }

    public List<Topic> get_subscribed_topics(String username) {
        return (this.users.containsKey(username) ?
                new ArrayList<>(this.users.get(username).getTopics().keySet())
                : null
        );
    }

    public Map<Topic, Long> get_topics(String username) {
        return (this.users.containsKey(username) ?
                this.users.get(username).getTopics()
                : null
        );
    }
}

class User {
    private String username;
    private String password;
    private Map<Topic, Long> subscribed_topics;

    // CONSTRUCTOR

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.subscribed_topics = new HashMap<>();
    }

    public synchronized Map<Topic, Long> getTopics() {
        return this.subscribed_topics;
    }

    public synchronized void setTopics(List<Topic> topics) {
        long subscription = System.nanoTime();

        // add new topics
        for (Topic t : topics){
            if (!this.subscribed_topics.containsKey(t)){
                this.subscribed_topics.put(t, subscription);
            }
            // if the topics map already contains the topic it doesn't update the timestamp, the old one prevails
        }
        // remove topics that aren't part of the list
        for (Topic t : this.subscribed_topics.keySet()){
            if (!topics.contains(t)){
                this.subscribed_topics.remove(t);
            }
        }
    }

    public synchronized boolean is_auth(String password){
        return this.password.equals(password);
    }
}
