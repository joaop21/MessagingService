package Application;

import util.FSDwitter;
import util.Post;
import util.Topic;

import java.util.*;

public class FSDwitterImpl implements FSDwitter {
    private Map<String, User> users;
    private Map<Topic, List<Post>> posts;

    public FSDwitterImpl(Map<String, User> users, Map<Topic, List<Post>> posts) {
        this.users = users;
        this.posts = posts;
    }

    public FSDwitterImpl() {
        this.users = new HashMap<>();
        this.users.put("joao", new User("joao","joaopass"));
        this.users.put("henrique", new User("henrique","henriquepass"));

        this.posts = new HashMap<>();
        this.posts.put(Topic.NEWS, new LinkedList<>());
        this.posts.put(Topic.SPORTS, new LinkedList<>());
        this.posts.put(Topic.CULTURE, new LinkedList<>());
        this.posts.put(Topic.PEOPLE, new LinkedList<>());
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<Topic, List<Post>> getPosts() {
        return posts;
    }

    public void setPosts(Map<Topic, List<Post>> posts) {
        this.posts = posts;
    }


    @Override
    public List<Post> get_10_recent_posts(String user) {
        return null;
    }

    @Override
    public List<Topic> get_topics(String username) {
        return (this.users.containsKey(username) ? (List<Topic>) this.users.get(username).getTopics().keySet() : null);
    }

    @Override
    public boolean make_post(Post p) {
        for(Topic t : p.getTopics())
            this.posts.get(t).add(p);
        return true;
    }

    @Override
    public boolean set_topics(String username, Map<Topic, Long> topics) {
        this.users.get(username).setTopics(topics);
        return false;
    }

    @Override
    public boolean is_auth(String username, String password) {
        return this.users.containsKey(username) && this.users.get(username).getPassword().equals(password);
    }
}
