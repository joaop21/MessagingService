package Application;

import java.util.List;

public class Post {
    private String user;
    private long date; // in Milis
    private String post;
    private List<Topic> topics;

    public Post(String user, long date, String post, List<Topic> topics) {
        this.user = user;
        this.date = date;
        this.post = post;
        this.topics = topics;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}