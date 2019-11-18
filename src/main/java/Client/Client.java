package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import Middleware.MiddlewareFacade;

public class Client {

    public static void initializeChat(BufferedReader sin, int port, MiddlewareFacade midd) throws IOException {
        System.out.println("------ Welcome to MessagingService! ------\n\n");
        System.out.println("Select an option by typing its number:");
        System.out.println("1) Login");
        System.out.println("2) Register");

        String option = sin.readLine();
        readOptionInitial(sin, option, midd);
    }

    public static void readOptionInitial(BufferedReader sin, String option, MiddlewareFacade midd) throws IOException {
        switch (option) {
        case "1":
            showLogin(sin, midd);
            break;
        case "2":
            System.out.print("Register chosen\n");
            showRegister(sin, midd);
            break;
        default:
            System.out.println("Invalid option. Please try again.");
            String newOption = sin.readLine();
            readOptionInitial(sin, newOption, midd);
            break;
        }
    }

    public static void showLogin(BufferedReader sin, MiddlewareFacade midd) throws IOException {
        System.out.println("- Login:");
        System.out.print("Username: ");
        String username = sin.readLine();
        System.out.print("\nPassword: ");
        String password = sin.readLine();
        System.out.println("\nTrying to login...");

        // Operation op = new Operation("login", username, password);
        // midd.sendClientMessage(op.serialize());
        midd.sendClientMessage("login;" + username + ";" + password);

        // wait for answer??
    }

    private static void showRegister(BufferedReader sin, MiddlewareFacade midd) throws IOException {
        System.out.print("- Register:\n");
        System.out.print("Username: ");
        String username = sin.readLine();
        System.out.print("\nPassword: ");
        String password = sin.readLine();
        System.out.println("\nTrying to register...");

        // Operation op = new Operation("register", username, password);
        // midd.sendClientMessage(op.serialize());
        midd.sendClientMessage("register;" + username + ";" + password);

        // wait for answer??
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        MiddlewareFacade midd = new MiddlewareFacade(port);
        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        try {
            initializeChat(sin, port, midd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}