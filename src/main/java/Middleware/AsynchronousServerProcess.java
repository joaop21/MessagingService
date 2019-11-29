package Middleware;

import Operations.Operation;
import Operations.Post.Post;
import Operations.Post.PostType;
import Operations.Reply.Response;
import Operations.Request.Request;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.concurrent.*;

class AsynchronousServerProcess extends Thread{
    private int port;
    private int[] network;

    private ManagedMessagingService mms;
    private Serializer response_serializer;
    private Serializer request_serializer;
    private Serializer post_serializer;
    private Serializer message_serializer;

    private CausalDelivery cd;

    /* data struct that keeps FIFO order on receiving Operations from clients */
    private ConcurrentQueue<Tuple<Integer, Operation>> clients_messages = new ConcurrentQueue<Tuple<Integer, Operation>>();
    /* data struct that respects Causal Delivery order on receiving Data from servers */
    private ConcurrentQueue<Operation> servers_messages = new ConcurrentQueue<Operation>();
    /* data struct that keeps FIFO order on receiving Data from servers */
    private ConcurrentQueue<Message<Operation>> data_to_sync = new ConcurrentQueue<Message<Operation>>();

    /**
     * Parameterized constructor that initializes an instance of AsynchronousServerProcess.
     *
     * @param p The port where the server will operate.
     * @param net An array containing ints representing the ports that the servers are running.
     * */
    AsynchronousServerProcess(int p, int[] net){
        this.port = p;
        this.network = net;

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncServerProcess", Address.from(this.port), new MessagingConfig());

        // Initializes Serializers capable of encode and decode Objects
        this.response_serializer = new SerializerBuilder().addType(Response.class).build();
        this.request_serializer = new SerializerBuilder().addType(Request.class).build();
        this.post_serializer = new SerializerBuilder().addType(Post.class).build();
        this.message_serializer = new SerializerBuilder().addType(Message.class).build();

        this.cd = new CausalDelivery(p,net,this);
        new Thread(this.cd).start();
    }

    /**
     * This method is the thread work while it's running.
     * Registers the fundamental handlers for events.
     * */
    @Override
    public void run() {
        // Thread pool for execution
        ExecutorService e = Executors.newFixedThreadPool(1);

        // handler for receiving requests, they're only sent from clients
        this.mms.registerHandler("Request", (a,b) ->{
            addClientMessage(a.port(), new Operation((Request) this.request_serializer.decode(b)));
        }, e);

        // handler for receiving posts, they're only sent from clients
        this.mms.registerHandler("Post", (a,b) ->{
            Post p = this.post_serializer.decode(b);

            // if is not a login than it changes data, so we have to sync servers
            if(!(p.getPostType() == PostType.LOGIN))
                this.cd.sendMessageToServers(new Operation(p));

            addClientMessage(a.port(), new Operation(p));
        }, e);

        // handler for receiving data from other servers
        this.mms.registerHandler("DataToSync", (a,b) ->{
            Message<Operation> msg = this.message_serializer.decode(b);
            this.data_to_sync.add(msg);
        }, e);

        this.mms.start().
                thenRun(() -> {
                    System.out.println("["+this.port+"]: Messaging Service Started");
                });
    }

    /**
     * Sends Message to all adjacent servers.
     *
     * @param msg The message to be sent.
     *
     * */
    void sendMessageToServers(Message<Operation> msg){
        for (int server_port : this.network)
            if(server_port!=this.port)
                this.mms.sendAsync(Address.from(server_port), "DataToSync", this.message_serializer.encode(msg));
    }

    /**
     * Sends a response to a specific client.
     *
     * @param client_port The port where the client is.
     * @param resp The message to be sent.
     *
     * */
    void sendMessageToClient(int client_port, Response resp){
            this.mms.sendAsync(Address.from(client_port), "Response", this.response_serializer.encode(resp));
    }

    /**
     * Method that consumes the head of the client messages queue when its available.
     *
     * @return Tuple<Integer, Operation> Tuple with port and operation.
     * */
    Tuple<Integer, Operation> getClientMessage(){
        return (Tuple<Integer, Operation>) this.clients_messages.poll();
    }

    /**
     *  Method that puts a tuple in the client messages queue to be consumed
     *
     * @param client_port port from the source client
     * @param op Message that will be added to the list
     * */
    private void addClientMessage(int client_port, Operation op){
        this.clients_messages.add(new Tuple<Integer, Operation>(client_port, op));
    }

    /**
     * Method that consumes the head of the server messages queue when its available.
     *
     * @return Operation Operation from client on other server.
     * */
    Operation getServerMessage(){
        return (Operation) this.servers_messages.poll();
    }

    /**
     *  Method that puts an operation in the server messages queue to be consumed.
     *
     * @param op Operation that will be added to the list.
     * */
    void addServerMessage(Operation op){
        this.servers_messages.add(op);
    }

    /**
     * Method that consumes the head of the messages to sync queue when its available.
     *
     * @return Tuple<Integer, Operation> Tuple with port and operation.
     * */
    Message<Operation> getDataToSync(){
        return (Message<Operation>) this.data_to_sync.poll();
    }

    /**
     *  Method that puts a tuple in the client messages queue to be consumed
     *
     * @param msg Message exchanged between servers.
     * */
    private void addDataToSync(Message<Operation> msg){
        this.data_to_sync.add(msg);
    }
}
