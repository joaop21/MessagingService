package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import Middleware.MiddlewareFacade;

import util.*;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

public class Client {

    static Serializer serializer = new SerializerBuilder().addType(Operation.class).build();

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

        String[] args = { username, password };
        Operation op = new Operation(OperationType.LOGIN, args);
        midd.sendClientMessage(serializer.encode(op));

        // wait for answer??
    }

    private static void showRegister(BufferedReader sin, MiddlewareFacade midd) throws IOException {
        System.out.print("- Register:\n");
        System.out.print("Username: ");
        String username = sin.readLine();
        System.out.print("\nPassword: ");
        String password = sin.readLine();
        System.out.println("\nTrying to register...");

        String[] args = { username, password };
        Operation op = new Operation(OperationType.REGISTER, args);
        midd.sendClientMessage(serializer.encode(op));

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