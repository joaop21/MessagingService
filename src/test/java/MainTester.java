import Middleware.Tuple;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

/**
 * This class is for testing the current code.
 * In the future it will be deleted.
 * */
public class MainTester extends Thread{
    private static ServerMiddleware serv_midd;

    private MainTester(ServerMiddleware midd){
        serv_midd = midd;
    }

    private CompletableFuture<Void> receiveMessagesServer() {
        try {
            System.out.println("receiveMessagesServer()");
            serv_midd.getServerMessage()
                    .thenCompose((line) -> {
                        System.out.println(line);
                        return receiveMessagesServer();
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    private static CompletableFuture<Void> receiveMessagesClient() {
        try {
            System.out.println("receiveMessagesClient()");
            serv_midd.getClientMessage()
                    .thenCompose((line) -> {
                        Tuple t = (Tuple)line;
                        System.out.println(t.getFirst() +": "+ t.getSecond());
                        return receiveMessagesClient();
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void run() {
        receiveMessagesServer();
        receiveMessagesClient();
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        serv_midd = new ServerMiddleware(port);
        new Thread(new MainTester(serv_midd)).start();

        Runnable task2 = () -> { receiveMessagesClient();};
        new Thread(task2).start();

        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        try {
            String current;
            while ((current = sin.readLine()) != null) {
                serv_midd.sendMessageToServer(current);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
