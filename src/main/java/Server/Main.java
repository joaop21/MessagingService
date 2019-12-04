package Server;

public class Main {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        FSDwitterSkeleton skeleton = new FSDwitterSkeleton(port);
    }
}
