package Middleware;

import java.util.concurrent.CompletableFuture;

public interface Middleware {
    CompletableFuture<Object> getServerMessage() throws InterruptedException;
    CompletableFuture<Object> getClientMessage() throws InterruptedException;
}
