package Middleware;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class ServerMiddleware implements Middleware{
    private CausalDelivery cd;
    /** Messages ordered from another servers **/
    private Queue<Message> ordered_messages_servers = new LinkedList<>();
    /** Messages ordered from clients **/
    private Queue<Message> messages_clients = new LinkedList<>();

    /**
     * Parameterized constructor that initializes an instance of MiddlewareFacade.
     *
     * @param p The port where the server will operate.
     * */
    public ServerMiddleware(int p){
        int[] network = new int[]{12345, 23456, 34567, 45678, 56789};
        this.cd = new CausalDelivery(p,this, network);
    }

    /**
     * Blocking method that consumes the head of the server messages linked list when its available
     *
     * @return Object Object that was exchanged in messages.
     * */
    @Override
    public synchronized CompletableFuture<Object> getServerMessage() throws InterruptedException {
        while(this.ordered_messages_servers.size() == 0)
            wait();

        return CompletableFuture.completedFuture(this.ordered_messages_servers.poll().getObject());
    }

    /**
     * Blocking method that consumes the head of the client messages linked list when its available
     *
     * @return Object Object that was exchanged in messages.
     * */
    @Override
    public synchronized CompletableFuture<Object> getClientMessage() throws InterruptedException {
        while(this.ordered_messages_servers.size() == 0)
            wait();

        return CompletableFuture.completedFuture(this.ordered_messages_servers.poll().getObject());
    }

    /**
     *  Method that puts an ordered message in the server messages List to be consumed
     *
     * @param msg Message that will be added to the list
     * */
    synchronized void addServerMessage(Message msg){
        this.ordered_messages_servers.add(msg);
        // notifies possible blocked thread
        notify();
    }
}
