package Middleware;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class MiddlewareFacade {
    private CausalDelivery cd;
    private Queue<Message> ordered_messages = new LinkedList<>();
    private int server_port;

    /**
     * Parameterized constructor that initializes an instance of MiddlewareFacade.
     *
     * @param p The port where the server will operate.
     * */
    public MiddlewareFacade(int p){
        int[] network = new int[]{12345, 23456, 34567, 45678, 56789};
        //this.cd = new CausalDelivery(p,this, network);

        Random rand = new Random();
        this.server_port = network[rand.nextInt(5)];
    }

    /**
     *  Method that puts an ordered message in the List to be consumed
     *
     * @param msg Message that will be added to the list
     * */
    /*synchronized void addMessage(Message msg){
        this.ordered_messages.add(msg);
        // notifies possible blocked thread
        notify();
    }*/

    /**
     * Blocking method that consumes the head of the linked list when its available
     *
     * @return Object Object that was exchanged in messages.
     * */
    public synchronized CompletableFuture<Object> getMessage() throws InterruptedException {
        while(this.ordered_messages.size() == 0)
            wait();

        return CompletableFuture.completedFuture(this.ordered_messages.poll().getObject());
    }

    /**
     * Method that send message to other servers
     *
     * @param o Object inside the message
     * */
    /*public void sendServerMessage(Object o){
        this.cd.sendServerMessage(o);
    }*/

    /**
     * Method that send message to other servers
     *
     * @param o Object inside the message
     * */
    public void sendClientMessage(Object o){
        //this.cd.sendClientMessage(o, this.server_port);
    }
}
