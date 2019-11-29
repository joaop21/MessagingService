package Operations.Post;

public enum PostType {
    LOGIN(1), MESSAGE(2), TOPICS(3);

    public int key;

    PostType(int key) {
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}
