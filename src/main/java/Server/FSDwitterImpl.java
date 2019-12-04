package Server;

import Application.FSDwitter;
import Application.Post;
import Application.Topic;
import Application.User;

import java.util.*;

public class FSDwitterImpl implements FSDwitter {
    private Map<String, User> users;
    private Map<Topic, Deque<Post>> posts;

    public FSDwitterImpl(Map<String, User> users, Map<Topic, Deque<Post>> posts) {
        this.users = users;
        this.posts = posts;
    }

    public FSDwitterImpl() {
        this.users = new HashMap<>();
        this.users.put("joao", new User("joao","joaopass"));
        this.users.put("henrique", new User("henrique","henriquepass"));

        this.posts = new HashMap<>();
        this.posts.put(Topic.NEWS, new ArrayDeque<>());
        this.posts.put(Topic.SPORTS, new ArrayDeque<>());
        this.posts.put(Topic.CULTURE, new ArrayDeque<>());
        this.posts.put(Topic.PEOPLE, new ArrayDeque<>());
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<Topic, Deque<Post>> getPosts() {
        return posts;
    }

    public void setPosts(Map<Topic, Deque<Post>> posts) {
        this.posts = posts;
    }


    @Override
    public List<Post> get_10_recent_posts(String username) {
        User user = users.get(username);
        List<Post> subscribed_posts = new ArrayList<>();
        int num_posts = 0;

        for (Topic t : user.getTopics().keySet()){
            long subscribed_date = user.getTopics().get(t);

            for (Post post : this.posts.get(t)){
                if (post.getDate() > subscribed_date){
                    subscribed_posts.add(post);
                    num_posts++;
                }
            }
        }

        Collections.sort(subscribed_posts, new Comparator<Post>(){
            public int compare(Post p1, Post p2){
                return Double.compare(p2.getDate(),p1.getDate());
          }
        });

        if(num_posts>9) return new ArrayList<>(subscribed_posts.subList(0, 9));
        else return new ArrayList<>(subscribed_posts.subList(0, num_posts));
    }

    @Override
    public List<Topic> get_topics(String username) {
        return (this.users.containsKey(username) ? 
            new ArrayList<>(this.users.get(username).getTopics().keySet()) 
            : null
        );
    }

    @Override
    public boolean make_post(Post p) {
        for(Topic t : p.getTopics()){
            Deque<Post> posts = this.posts.get(t);
            posts.addFirst(p);
            if (posts.size() > 10)
                posts.removeLast();
        }
        return true;
    }

    @Override
    public boolean set_topics(String username, List<Topic> topics) {
        long subscription = new Date().getTime();
        User user = this.users.get(username);
        Map<Topic,Long> topicMap = user.getTopics();
        for (Topic t : topics){
            if (topicMap.containsKey(t)){
                topicMap.replace(t, subscription);
            }
            else topicMap.put(t, subscription);
        }
        return true;
    }

    @Override
    public boolean is_auth(String username, String password) {
        return this.users.containsKey(username) && this.users.get(username).getPassword().equals(password);
    }
}
