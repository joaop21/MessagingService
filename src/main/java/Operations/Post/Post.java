package Operations.Post;

public class Post {
    private final PostType postType;
    private final Object post;

    public Post(PostLogin pl){
        this.postType = PostType.LOGIN;
        this.post = pl;
    }

    public Post(PostMessage pm){
        this.postType = PostType.MESSAGE;
        this.post = pm;
    }

    public Post(PostTopics pts){
        this.postType = PostType.TOPICS;
        this.post = pts;
    }

    public PostType getPostType() {
        return postType;
    }

    public Object getPost() {
        return post;
    }
}
