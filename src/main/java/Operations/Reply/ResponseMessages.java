package Operations.Reply;

import java.util.List;

import Application.Post;


public class ResponseMessages{
    private List<Post> posts;

    public ResponseMessages(List<Post> posts) {
        this.posts = posts;
    }

    public List<Post> getPosts() {
        return posts;
    }    
}