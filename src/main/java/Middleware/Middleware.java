package Middleware;

import java.util.concurrent.CompletableFuture;

public interface Middleware {
    CompletableFuture<Object> getServerMessage() throws InterruptedException;
    CompletableFuture<Tuple> getClientMessage() throws InterruptedException;
    void sendMessageToClient(int p, Object o);
    void sendMessageToServers(Object o);
}
