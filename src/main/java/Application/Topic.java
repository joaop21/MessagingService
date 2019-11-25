package Application;

public enum Topic {
    NEWS(1), SPORTS(2), CULTURE(3), PEOPLE(4);

    public int key;
    Topic(int key) {
        this.key = key;
    }

    public int getKey(){
        return key;
    }
}