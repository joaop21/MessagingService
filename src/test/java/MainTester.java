import Middleware.ServerMiddleware;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

/**
 * This class is for testing the current code.
 * In the future it will be deleted.
 * */
public class MainTester extends Thread{
    private static ServerMiddleware serv_midd;

    MainTester(ServerMiddleware midd){
        serv_midd = midd;
    }

    static CompletableFuture<Void> receiveMessages() {
        try {
            serv_midd.getServerMessage()
                    .thenCompose((line) -> {
                        System.out.println(line);
                        return receiveMessages();
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void run() {
        receiveMessages();
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        serv_midd = new ServerMiddleware(port);
        new Thread(new MainTester(serv_midd)).start();

        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        try {
            String current;
            while ((current = sin.readLine()) != null) {
                serv_midd.sendMessageToServers(current);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
