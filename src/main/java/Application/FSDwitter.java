package Application;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FSDwitter {
    private Map<String, User> users;
    private Map<Topic, Collection<Post>> posts;

    public FSDwitter(Map<String, User> users, Map<Topic, Collection<Post>> posts) {
        this.users = users;
        this.posts = posts;
    }

    public FSDwitter() {
        this.users = new HashMap<>();
        this.posts = new HashMap<>();
    }
    

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<Topic, Collection<Post>> getPosts() {
        return posts;
    }

    public void setPosts(Map<Topic, Collection<Post>> posts) {
        this.posts = posts;
    }
}
