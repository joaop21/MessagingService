package Application;

public enum Topic {
    NEWS, SPORTS, CULTURE, PEOPLE;

    private static Topic[] list = Topic.values();

    public static Topic getByKey(int key) {
        return list[key-1];
    }

    public static Topic[] getList() {
        return list;
    }

    public static int getSize() {
        return list.length;
    }
}