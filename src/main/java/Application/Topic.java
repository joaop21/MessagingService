package Application;

public enum Topic {
    NEWS, SPORTS, CULTURE, PEOPLE, ART, HEALTH, FITNESS, FASHION, TRAVEL, FOOD, MUSIC, DIY, LIFESTYLE;

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