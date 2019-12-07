package Server;

import Application.*;

import java.util.*;

public class FSDwitterImpl implements FSDwitter {
    private Users users;
    private Posts posts;

    public FSDwitterImpl(Users users, Posts posts) {
        this.users = users;
        this.posts = posts;
    }

    public FSDwitterImpl() {
        this.users = new Users();
        this.posts = new Posts();
    }


    @Override
    public List<Post> get_10_recent_posts(String username) {
        Map<Topic,Long> topics = this.users.get_topics(username);
        return this.posts.get_10_recent_posts(topics);
    }


    @Override
    public boolean make_post(Post p) {
        if(this.users.contains(p.getUser())){
            return this.posts.make_post(p);
        }
        return false;
    }


    @Override
    public List<Topic> get_topics(String username) {
        return this.users.get_subscribed_topics(username);
    }


    @Override
    public boolean set_topics(String username, List<Topic> topics) {
        return this.users.set_topics(username,topics);
    }


    @Override
    public boolean is_auth(String username, String password) {
        return this.users.is_auth(username, password);
    }
}
