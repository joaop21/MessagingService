package Middleware;

import java.util.LinkedList;
import java.util.Queue;

public class MiddlewareFacade {
    private CausalDelivery cd;
    private Queue<Message> ordered_messages = new LinkedList<>();

    /**
     * Parameterized constructor that initializes an instance of MiddlewareFacade.
     *
     * @param p The port where the server will operate.
     * */
    public MiddlewareFacade(int p){
        this.cd = new CausalDelivery(p,this);
    }

    /**
     *  Method that puts an ordered message in the List to be consumed
     *
     * @param msg Message that will be added to the list
     * */
    synchronized void addMessage(Message msg){
        this.ordered_messages.add(msg);
        // notifies possible blocked thread
        notify();
    }

    /**
     * Blocking method that consumes the head of the linked list when its available
     *
     * @return Object Object that was exchanged in messages.
     * */
    public synchronized Object getMessage() throws InterruptedException {
        while(this.ordered_messages.size() == 0)
            wait();

        return this.ordered_messages.poll().getObject();
    }

    /**
     * Method that send message to other servers
     *
     * @param o Object inside the message
     * */
    public void sendServerMessage(Object o){
        this.cd.sendServerMessage(o);
    }

    /**
     * Method that send message to other servers
     *
     * @param o Object inside the message
     * */
    public void sendClientMessage(Object o, int client_port){
        this.cd.sendClientMessage(o, client_port);
    }
}
