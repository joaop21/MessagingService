package Middleware;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class ClientMiddleware implements Middleware {
    /** Messages from servers**/
    private Queue<Message> messages_server = new LinkedList<>();
    private int server_to_contact;
    private AsynchronousProcess asp;

    /**
     * Parameterized constructor that initializes an instance of ClientMiddleware.
     *
     * @param p The port where the server will operate.
     * */
    public ClientMiddleware(int p){
        int[] network = new int[]{12345, 23456, 34567, 45678, 56789};

        Random rand = new Random();
        this.server_to_contact = network[rand.nextInt(5)];

        this.asp = new AsynchronousProcess(p, this, network);
    }

    /**
     * Blocking method that consumes the head of the server messages linked list when its available
     *
     * @return Object Object that was exchanged in messages.
     * */
    @Override
    public synchronized CompletableFuture<Object> getServerMessage() throws InterruptedException {
        while (this.messages_server.size() == 0)
            wait();

        return CompletableFuture.completedFuture(this.messages_server.poll().getObject());
    }

    /**
     *  Method that puts a message in the List to be consumed
     *
     * @param msg Message that will be added to the list
     * */
    synchronized void addMessage(Message msg){
        this.messages_server.add(msg);
        // notifies possible blocked thread
        notify();
    }

    /**
     * Method that send message to server
     *
     * @param o Object inside the message
     * */
    @Override
    public void sendMessageToServer(Object o) {
        // send directly to asyncprocess
        this.asp.sendMessageToServer(this.server_to_contact, new Message(this.server_to_contact, o, null));
    }
}
