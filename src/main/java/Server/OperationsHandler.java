package Server;

import Middleware.MiddlewareFacade;

import java.util.concurrent.CompletableFuture;

class OperationsHandler {
    private MiddlewareFacade midd;

    OperationsHandler(MiddlewareFacade mfac){
        this.midd = mfac;
    }

    CompletableFuture<Void> receiveOperations() {
        try {
            this.midd.getMessage()
                    .thenCompose((n) -> {
                        System.out.println("Message received");
                        // Here it has to handle the Operation received

                        return receiveOperations();
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }
}
