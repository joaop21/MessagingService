import Middleware.ClientMiddleware;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class ClientTester extends Thread {
    private static ClientMiddleware midd;

    public ClientTester(ClientMiddleware mfa){
        midd = mfa;
    }

    @Override
    public void run() {
        receiveMessages();
    }

    private CompletableFuture<Void> receiveMessages() {
        try {
            midd.getServerMessage()
                    .thenCompose((line) -> {
                        System.out.println(line);
                        return receiveMessages();
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        midd = new ClientMiddleware(port);

        new Thread(new ClientTester(midd)).start();

        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        try {
            String current;
            while ((current = sin.readLine()) != null) {
                midd.sendMessageToServer(current);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
