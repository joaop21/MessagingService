package Middleware;

import Application.Topic;
import Operations.Operation;
import Operations.OperationType;
import Operations.Post.*;
import Operations.Reply.*;
import Operations.Request.Request;
import Operations.Request.RequestMessages;
import Operations.Request.RequestTopics;
import Operations.Request.RequestType;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class AsynchronousServerProcess extends Thread{
    private int port;
    private int[] network;
    private boolean crash_recovery;
    private int num_server_recovered;

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
        this.crash_recovery = false;
        this.num_server_recovered = 0;

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncServerProcess", Address.from(this.port), new MessagingConfig());
        this.mms.start().
                thenRun(() -> {
                    System.out.println("["+this.port+"]: Messaging Service Started");
                });

        // Initializes Serializers capable of encode and decode Objects
        this.response_serializer = new SerializerBuilder()
                .withTypes(Response.class, ResponseMessages.class, ResponseTopics.class, Confirm.class, ResponseType.class,
                        Application.Post.class, Topic.class)
                .build();
        this.request_serializer = new SerializerBuilder()
                .withTypes(Request.class, RequestMessages.class, RequestTopics.class, RequestType.class)
                .build();
        this.post_serializer = new SerializerBuilder()
                .withTypes(Post.class, PostMessage.class, PostTopics.class, PostLogin.class, PostType.class,
                        Application.Post.class, Topic.class)
                .build();
        this.message_serializer = new SerializerBuilder()
                .withTypes(Message.class, Operation.class, OperationType.class, Post.class, PostMessage.class,
                        PostTopics.class, PostLogin.class, PostType.class, Application.Post.class, Topic.class)
                .build();

        this.cd = new CausalDelivery(p,net,this);
        new Thread(this.cd).start();
    }

    /**
     * Setter for variable crash_recovery.
     *
     * @param crash_recovery Boolean that tells if this server is recovering or not.
     */
    public void setCrash_recovery(boolean crash_recovery) {
        this.crash_recovery = crash_recovery;
        this.num_server_recovered = 0;
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
            System.out.println("["+this.port+"]: Received REQUEST from client " + a.port());
            addClientMessage(a.port(), new Operation((Request) this.request_serializer.decode(b)));
        }, e);

        // handler for receiving posts, they're only sent from clients
        this.mms.registerHandler("Post", (a,b) ->{
            if(!this.crash_recovery) {
                System.out.println("[" + this.port + "]: Received POST from client " + a.port());
                Post p = this.post_serializer.decode(b);

                // if is not a login than it changes data, so we have to sync servers
                if (!(p.getPostType() == PostType.LOGIN))
                    this.cd.sendMessageToServers(new Operation(p));

                addClientMessage(a.port(), new Operation(p));
            }
        }, e);

        // handler for receiving data from other servers
        this.mms.registerHandler("DataToSync", (a,b) ->{
            System.out.println("["+this.port+"]: Received DATA TO SYNC from server " + a.port());
            Message<Operation> msg = this.message_serializer.decode(b);
            this.data_to_sync.add(msg);
        }, e);

        // handler for receiving crashes from other servers
        this.mms.registerHandler("Crash", (a,b) ->{
            System.out.println("["+this.port+"]: Received CRASH CLOCK MAP from server " + a.port());
            Serializer s = new SerializerBuilder().build();
            Map<Integer,Integer> clock = s.decode(b);
            // pass to causal delivery
            ///
            ///
            ///
        }, e);

        // handler for receiving crashes from other servers
        this.mms.registerHandler("FinishedRecovery", (a,b) ->{
            this.num_server_recovered++;
            // check if process has terminated
            if(this.num_server_recovered == this.network.length)
                setCrash_recovery(false);
        }, e);
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
     * Sends Message to a specific server.
     *
     * @param msg The message to be sent.
     * @param server_port Port of the server.
     * */
    void sendMessageToServer(Message<Operation> msg, int server_port){
        this.mms.sendAsync(Address.from(server_port), "DataToSync", this.message_serializer.encode(msg));
    }

    /**
     * Sends Message of crash to all adjacent servers.
     *
     * @param clock The last captured clock.
     *
     * */
    void sendCrashToServers(Map<Integer,Integer> clock){
        Serializer s = new SerializerBuilder().build();
        for (int server_port : this.network)
            if(server_port!=this.port)
                this.mms.sendAsync(Address.from(server_port), "Crash", s.encode(clock));
    }

    /**
     * Sends Message of terminated recovery from crash to crashed servers.
     *
     * @param server_port Port from the server thar crashed.
     *
     * */
    void sendTerminatedCrashToServer(int server_port){
        Serializer s = new SerializerBuilder().build();
        this.mms.sendAsync(Address.from(server_port), "FinishedRecovery", s.encode("FinishedRecovery"));
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
