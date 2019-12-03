package Operations;

public enum OperationType {
    REQUEST(1), POST(2), RESPONSE(3);

    public int key;

    OperationType(int key) {
        this.key = key;
    }
    public int getKey(){
        return key;
    }
}
