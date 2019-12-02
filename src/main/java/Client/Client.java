package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Application.FSDwitter;
import Application.Post;
import Application.Topic;

public class Client {

    static FSDwitter app;
    static BufferedReader sin;

    private static void initializeChat() throws IOException {
        System.out.println("------ Welcome to MessagingService! ------\n\n");
        System.out.println("Select an option by typing its number:");
        System.out.println("1) Login");
        System.out.println("2) Register");

        String option = sin.readLine();
        readOptionInitial(option);
    }

    private static void readOptionInitial(String option) throws IOException {
        switch (option) {
            case "1":
                showLogin();
                break;
            case "2":
                System.out.print("Register chosen\n");
                showRegister();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
        String newOption = sin.readLine();
        readOptionInitial(newOption);
    }

    private static void showLogin() throws IOException {
        System.out.println("- Login:");
        System.out.print("Username: ");
        String username = sin.readLine();
        System.out.print("\nPassword: ");
        String password = sin.readLine();
        System.out.println("\nTrying to login...");

        if(!app.is_auth(username,password)){
            System.out.println("Wrong Credentials. Please try again.");
            showLogin();
        } else{
            showMainMenu(username);
        }
    }

    private static void showRegister() throws IOException {
        System.out.print("- Register:\n");
        System.out.print("Username: ");
        String username = sin.readLine();
        System.out.print("\nPassword: ");
        String password = sin.readLine();
        System.out.println("\nTrying to register...");

        System.out.println("Operation not implemented. Be patient.");
    }

    private static void showMainMenu(String username) throws IOException {
        System.out.println("What to do?");
        System.out.println("1) Make a New Post         2) Get 10 Most Recent Posts\n" +
                           "3) Show Subscribed Topics  4) Change Subscribed Topics");

        String option = sin.readLine();
        switch (option) {
            case "1":
                showCreatePost(username);
                break;
            case "2":
                System.out.println("The 10 Most Recent Post You Subscribed:");
                List<Post> posts =  app.get_10_recent_posts(username);
                for(Post p : posts)
                    p.toString();
                break;
            case "3":
                System.out.println("Subscribed Topics:");
                List<Topic> ts = app.get_topics(username);
                for(Topic t : ts)
                    t.toString();
                break;
            case "4":
                showChangeTopics(username);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    private static void  showCreatePost(String username) throws IOException {
        System.out.print("Message: ");
        String text = sin.readLine();

        System.out.println("1) NEWS       2) SPORTS");
        System.out.println("3) CULTURE    4) PEOPLE");

        System.out.print("Topics (separate by a space): ");
        String topics_line = sin.readLine();

        List<String> topics_string = new ArrayList(Arrays.asList(topics_line.split(" ")));
        List<Topic> topics = new ArrayList<>();
        for(String value : topics_string) {
            int topic_value = Integer.parseInt(value);
            switch(topic_value){
                case 1:
                    topics.add(Topic.NEWS);
                    break;
                case 2:
                    topics.add(Topic.SPORTS);
                    break;
                case 3:
                    topics.add(Topic.CULTURE);
                    break;
                case 4:
                    topics.add(Topic.PEOPLE);
                    break;
                default:
                    System.out.println("Bad input for topics.\n");
                    return;
            }
        }
        app.make_post(new Post(username, System.currentTimeMillis(),text,topics));
    }

    private static void showChangeTopics(String username) throws IOException {
        System.out.println("1) NEWS       2) SPORTS");
        System.out.println("3) CULTURE    4) PEOPLE");

        System.out.print("Topics (separate by a space): ");
        String topics_line = sin.readLine();

        List<String> topics_string = new ArrayList(Arrays.asList(topics_line.split(" ")));
        List<Topic> topics = new ArrayList<>();
        for(String value : topics_string) {
            int topic_value = Integer.parseInt(value);
            switch (topic_value) {
                case 1:
                    topics.add(Topic.NEWS);
                    break;
                case 2:
                    topics.add(Topic.SPORTS);
                    break;
                case 3:
                    topics.add(Topic.CULTURE);
                    break;
                case 4:
                    topics.add(Topic.PEOPLE);
                    break;
                default:
                    System.out.println("Bad input for topics.\n");
                    return;
            }
        }
        app.set_topics(username,topics);
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        // initializes ClientProcess

        app = new FSDwitterStub(port);

        sin = new BufferedReader(new InputStreamReader(System.in));

        try {
            initializeChat();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}