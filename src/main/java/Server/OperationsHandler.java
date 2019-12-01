package Server;

import Operations.Operation;

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
                        //
                        Operation op = (Operation) n;
                        switch (op.getType()){
                            case LOGIN:
                                System.out.println("Login");
                                break;
                            case REGISTER:
                                System.out.println("Register");
                                break;
                            case PUBLISH:
                                System.out.println("Publish");
                                break;
                            case TOPICS:
                                System.out.println("Topics");
                                break;
                            case GET10:
                                System.out.println("Get10");
                                break;
                            default:
                                System.out.println("Not Recognized");
                                break;
                        }

                        return receiveOperations();
                    });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }
}
