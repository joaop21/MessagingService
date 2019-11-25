package Operations.Request;

public enum RequestType {
    MESSAGES(1), TOPICS(2);

    public int key;

    RequestType(int key) {
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}
