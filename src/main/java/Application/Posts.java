package Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Posts {
    private Map<Topic, BoundedQueue<Post>> posts;

    public Posts(){
        this.posts = new HashMap<>();
        this.posts.put(Topic.NEWS, new BoundedQueue<>(10));
        this.posts.put(Topic.SPORTS, new BoundedQueue<>(10));
        this.posts.put(Topic.CULTURE, new BoundedQueue<>(10));
        this.posts.put(Topic.PEOPLE, new BoundedQueue<>(10));
    }

    synchronized void addTopic(Topic t){
        this.posts.put(t,new BoundedQueue<>(10));
    }

    public boolean make_post(Post p) {
        for(Topic t : p.getTopics()){
            p.setDate(System.nanoTime());
            if(!this.posts.containsKey(t))
                this.addTopic(t);

            BoundedQueue<Post> tagged_posts = this.posts.get(t);
            tagged_posts.add(p);
        }
        return true;
    }

    public List<Post> get_10_recent_posts(Map<Topic,Long> topics) {
        List<Post> subscribed_posts = new ArrayList<>();
        int num_posts = 0;

        for (Topic t : topics.keySet()){
            long subscribed_date = topics.get(t);

            if (this.posts.get(t) != null)
                for (Post post : this.posts.get(t).get()){
                    if (post.getDate() > subscribed_date){
                        subscribed_posts.add(post);
                        num_posts++;
                    }
                }
            else
                System.out.println("There are no posts for the topic "+t.name()+" yet! Do you want to be the first? :) \n");
        }

        Collections.sort(subscribed_posts, new Comparator<Post>(){
            public int compare(Post p1, Post p2){
                return Double.compare(p2.getDate(),p1.getDate());
            }
        });

        if(num_posts>9) return new ArrayList<>(subscribed_posts.subList(0, 9));
        else return new ArrayList<>(subscribed_posts.subList(0, num_posts));
    }
}
