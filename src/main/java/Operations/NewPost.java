package Operations;

public class NewPost{
    private String username;
    private String post;

    public NewPost(String username, String post) {
        this.username = username;
        this.post = post;
    }

    public String getUsername() {
        return username;
    }

    public String getPost() {
        return post;
    }

}