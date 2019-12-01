package Operations.Post;

public class Post {
    private PostType postType;
    private Object post;

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

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public Object getPost() {
        return post;
    }

    public void setPost(Object post) {
        this.post = post;
    }
}
