package Server;

import Application.FSDwitter;
import Application.Post;
import Application.Topic;
import Application.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FSDwitterImpl implements FSDwitter {
    private ConcurrentMap<String, User> users;
    private ConcurrentMap<Topic, Deque<Post>> posts;

    public FSDwitterImpl(ConcurrentMap<String, User> users, ConcurrentMap<Topic, Deque<Post>> posts) {
        this.users = users;
        this.posts = posts;
    }

    public FSDwitterImpl() {
        this.users = new ConcurrentHashMap<>();
        this.users.put("joao", new User("joao","joaopass"));
        this.users.put("henrique", new User("henrique","henriquepass"));

        this.posts = new ConcurrentHashMap<>();
        this.posts.put(Topic.NEWS, new ArrayDeque<>());
        this.posts.put(Topic.SPORTS, new ArrayDeque<>());
        this.posts.put(Topic.CULTURE, new ArrayDeque<>());
        this.posts.put(Topic.PEOPLE, new ArrayDeque<>());
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(ConcurrentMap<String, User> users) {
        this.users = users;
    }

    public Map<Topic, Deque<Post>> getPosts() {
        return posts;
    }

    public void setPosts(ConcurrentMap<Topic, Deque<Post>> posts) {
        this.posts = posts;
    }


    @Override
    public List<Post> get_10_recent_posts(String username) {
        if(users.containsKey(username)) {
            Map<Topic, Long> subscribed_topics = users.get(username).getTopics();
            List<Post> subscribed_posts = new ArrayList<>();

            for (Topic t : subscribed_topics.keySet()) {
                long subscribed_date = subscribed_topics.get(t);

                for (Post post : this.posts.get(t)) {
                    if (post.getDate() > subscribed_date) {
                        subscribed_posts.add(post);
                    }
                }
            }

            Collections.sort(subscribed_posts, new Comparator<Post>() {
                public int compare(Post p1, Post p2) {
                    return Double.compare(p2.getDate(), p1.getDate());
                }
            });

            return subscribed_posts.subList(0, 9);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Topic> get_topics(String username) {
        return (this.users.containsKey(username) ? 
            new ArrayList<>(this.users.get(username).getTopics().keySet()) 
            : new ArrayList<>()
        );
    }

    @Override
    public boolean make_post(Post p) {
        for(Topic t : p.getTopics()){
            synchronized (this) {
                Deque<Post> posts = this.posts.get(t);
                posts.addFirst(p);
                if (posts.size() > 10)
                    posts.removeLast();
            }
        }
        return true;
    }

    @Override
    public boolean set_topics(String username, List<Topic> topics) {
        this.users.get(username).setTopics(topics);
        return true;
    }

    @Override
    public boolean is_auth(String username, String password) {
        return this.users.containsKey(username) && this.users.get(username).getPassword().equals(password);
    }
}
