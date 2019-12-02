package Operations.Post;

import Application.Post;

public class PostMessage{
    private Post post;

    public PostMessage(Post p) {
        this.post = p;
    }

    public Post getPost() {
        return this.post;
    }
    
}