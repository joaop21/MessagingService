package Middleware;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerMiddleware implements Middleware{
    /** Messages ordered from another servers **/
    private Queue<Message> ordered_messages_servers = new LinkedList<>();
    /** Messages ordered from clients **/
    private Queue<Tuple<Integer,Message>> messages_clients = new LinkedList<>();

    private CausalDelivery cd;
    private AsynchronousServerProcess asp;

    /** Locking policies because there is 2 different conditions*/
    private final Lock lock = new ReentrantLock();
    private final Condition no_server_messages = lock.newCondition();
    private final Condition no_client_messages = lock.newCondition();

    /**
     * Parameterized constructor that initializes an instance of ServerMiddleware.
     *
     * @param p The port where the server will operate.
     * */
    public ServerMiddleware(int p){
        int[] network = new int[]{12345, 23456, 34567, 45678, 56789};
        this.cd = new CausalDelivery(p,this, network);
        this.asp = null;
    }

    /**
     * Setter to change AsynchronousServerProcess
     *
     * @param aspn AsynchronousServerProcess class to be used
     * */
    void setAsp(AsynchronousServerProcess aspn){
        this.asp = aspn;
    }

    /**
     * Blocking method that consumes the head of the server messages linked list when its available
     *
     * @return Object Object that was exchanged in messages.
     * */
    @Override
    public CompletableFuture<Object> getServerMessage() throws InterruptedException {
        this.lock.lock();
        try {
            while (this.ordered_messages_servers.size() == 0)
                this.no_server_messages.await();

            return CompletableFuture.completedFuture(this.ordered_messages_servers.poll().getObject());
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Blocking method that consumes the head of the client messages linked list when its available
     *
     * @return Object Object that was exchanged in messages.
     * */
    public CompletableFuture<Tuple> getClientMessage() throws InterruptedException {
        this.lock.lock();
        try {
            while (this.messages_clients.size() == 0)
                this.no_client_messages.await();

            Tuple<Integer, Message> tim = this.messages_clients.poll();
            return CompletableFuture.completedFuture(new Tuple<Integer,Object>(tim.getFirst(), tim.getSecond().getObject()));
        } finally {
            this.lock.unlock();
        }
    }

    /**
     *  Method that puts an ordered message in the server messages List to be consumed
     *
     * @param msg Message that will be added to the list
     * */
    void addServerMessage(Message msg){
        this.lock.lock();
        try {
            this.ordered_messages_servers.add(msg);
            // notifies possible blocked threads
            this.no_server_messages.signal();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     *  Method that puts an message in the client messages List to be consumed
     *
     * @param p port from the source client
     * @param msg Message that will be added to the list
     * */
    void addClientMessage(int p,Message msg){
        this.lock.lock();
        try {
            this.messages_clients.add(new Tuple<>(p,msg));
            // notifies possible blocked threads
            this.no_client_messages.signal();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Method that send message to other servers
     *
     * @param o Object inside the message
     * */
    @Override
    public void sendMessageToServer(Object o){
        this.cd.sendMessageToServers(o);
    }

    /**
     * Method that send message to a specific client
     *
     * @param o Object inside the message
     * */
    public void sendMessageToClient(int p, Object o){
        // send directly to asyncprocess
        this.asp.sendMessageToClient(p,new Message(p,o,null));
    }
}
