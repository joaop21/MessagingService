package Operations.Reply;

public enum ResponseType {
    MESSAGES(1), TOPICS(2), CONFIRM(3);

    public int key;

    ResponseType(int key) {
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}
