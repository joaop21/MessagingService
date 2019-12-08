package Application;

import java.util.Date;
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

    public String toString(){
        StringBuilder post = new StringBuilder();
        post.append(user + " on " + (new Date(date)).toString() + ": \n");
        post.append(post + "\n");
        post.append("Topics: ");
        for(Topic t : topics)
            post.append('#'+t.toString()+" ");
        post.append("\n------------------ // ------------------\n");

        return post.toString();
    }
}