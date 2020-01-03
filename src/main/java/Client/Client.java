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
                String newOption = sin.readLine();
                readOptionInitial(newOption);
                break;
        }
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
            showMainMenu(username, true);
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

    private static void showMainMenu(String username, boolean first_try) throws IOException {
        if (first_try){
            System.out.println("Welcome back "+username+"!");
        }
        System.out.println("What do you want to do?");
        System.out.println("1) Make a New Post         2) Get 10 Most Recent Posts\n" +
                        "3) Show Subscribed Topics  4) Change Subscribed Topics");

        String option = sin.readLine();
        switch (option) {
            case "1":
                showCreatePost(username);
                break;
            case "2":
                show10MostRecentPosts(username);
                break;
            case "3":
                showSubscribedTopics(username);
                break;
            case "4":
                showChangeTopics(username);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                option = sin.readLine();
                showMainMenu(username,false);
                break;
        }
    }

    private static void showCreatePost(String username) throws IOException {
        System.out.print("\nMessage: ");
        String text = sin.readLine();

        // print topics
       int i = 1;
       for (Topic t : Topic.getList()){
           System.out.println(i+") "+t.toString());
           i++;
       }

        System.out.print("Topics (separated by a space): ");
        String topics_line = sin.readLine();

        List<String> topics_string = new ArrayList<>(Arrays.asList(topics_line.split(" ")));
        List<Topic> topics = new ArrayList<>();
        for(String value : topics_string) {
            int topic_value = Integer.parseInt(value);
            if (topic_value < Topic.getSize() && topic_value > 0){
               topics.add(Topic.getByKey(topic_value));
            }
            else {
                System.out.println("Bad input for topics.\n");
                showMainMenu(username, false);
            }
        }

        app.make_post(new Post(username, System.currentTimeMillis(),text,topics));

        showMainMenu(username, false);
    }

    private static void show10MostRecentPosts(String username) throws IOException {
        System.out.println("\nThe 10 most recent posts with the topics you subscribe:\n");
        List<Post> posts =  app.get_10_recent_posts(username);
        for(Post p : posts)
            System.out.println(p.toString());

        showMainMenu(username, false);
    }

    private static void showSubscribedTopics(String username) throws IOException {
        System.out.println("\nSubscribed Topics:");
        List<Topic> ts = app.get_topics(username);
        if (ts == null || ts.isEmpty())
            System.out.println("You have no subscribed topics!\n");
        else
            for(Topic t : ts)
                System.out.println(t.toString());

        showMainMenu(username, false);
    }

    private static void showChangeTopics(String username) throws IOException {
         // print topics
       System.out.println("\nAvailable topics: ");
       int i = 1;
       for (Topic t : Topic.getList()){
           System.out.println(i+") "+t.toString());
           i++;
       }

        System.out.print("Topics (separated by a space): ");
        String topics_line = sin.readLine();

        List<String> topics_string = new ArrayList<>(Arrays.asList(topics_line.split(" ")));
        List<Topic> topics = new ArrayList<>();
        for(String value : topics_string) {
            int topic_value = Integer.parseInt(value);
            if (topic_value < Topic.getSize() && topic_value > 0){
               topics.add(Topic.getByKey(topic_value));
            }
            else {
                System.out.println("Bad input for topics.\n");
                showMainMenu(username, false);
            }
        }
        app.set_topics(username,topics);

        showMainMenu(username, false);
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