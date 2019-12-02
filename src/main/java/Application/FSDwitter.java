package Application;

import java.util.List;

public interface FSDwitter {
    List<Post> get_10_recent_posts(String username);
    List<Topic> get_topics(String username);
    boolean make_post(Post p);
    boolean set_topics(String username, List<Topic> topics);
    boolean is_auth(String username, String password);
}
