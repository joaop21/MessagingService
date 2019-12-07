package Application;

import java.util.List;

public interface FSDwitter {
    /**
     * Method that return the 10 recent posts of the topics that a user subscribed.
     *
     * @param username Username of the user.
     *
     * @return List<Post> List containing the 10 recent posts.
     * */
    List<Post> get_10_recent_posts(String username);

    /**
     * Method that get the subscribed topics from an user.
     *
     * @param username Username od the user.
     *
     * @return List<Topic> List of subscribed topics.
     * */
    List<Topic> get_topics(String username);

    /**
     * Method that inserts a post in the data structure if it's valid.
     *
     * @param p Post to be inserted.
     *
     * @return boolean That represents if the operation was or wasn't successful.
     * */
    boolean make_post(Post p);

    /**
     * Method that set the subscribed topics for an user.
     *
     * @param username Username of the user that subscribes the topics.
     * @param topics List of the topics to subscribe.
     *
     * @return boolean To check if the operation was successful.
     * */
    boolean set_topics(String username, List<Topic> topics);

    /**
     * Method that checks the credentials of an user, for him to login.
     *
     * @param username Username of the user.
     * @param password Password of the user.
     *
     * @return boolean Success or failure of the login.
     * */
    boolean is_auth(String username, String password);
}
