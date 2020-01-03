package Middleware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Journal.Journal;
import Application.Topic;
import Operations.Operation;
import Operations.OperationType;
import Operations.Post.Post;
import Operations.Post.PostLogin;
import Operations.Post.PostMessage;
import Operations.Post.PostTopics;
import Operations.Post.PostType;
import Operations.Reply.Confirm;
import Operations.Reply.Response;
import Operations.Reply.ResponseMessages;
import Operations.Reply.ResponseTopics;
import Operations.Reply.ResponseType;
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

class AsynchronousServerProcess extends Thread{
    /* Information of this server */
    private int port;
    private int[] network;

    /* Information for crashes treatment */
    private boolean crash_recovery;
    private int num_server_recovered;
    private Map<Integer,Integer> last_clock;
    private Map<Integer,Integer> messages_to_recover;
    private List<Message<Operation>> delayed_messages = new LinkedList<>();

    /* Information for message passing */
    private ManagedMessagingService mms;
    private Serializer response_serializer;
    private Serializer request_serializer;
    private Serializer post_serializer;
    private Serializer message_serializer;

    /* Information for consistent views*/
    private CausalDelivery cd;
    private Journal journal;
    private List<Message<Operation>> sent_messages;

    /* data struct that keeps FIFO order on receiving Operations from clients */
    private ConcurrentQueue<Tuple<Integer, Operation>> clients_messages = new ConcurrentQueue<>();
    /* data struct that respects Causal Delivery order on receiving Data from servers */
    private ConcurrentQueue<Operation> servers_messages = new ConcurrentQueue<>();
    /* data struct that keeps FIFO order on receiving Data from servers */
    private ConcurrentQueue<Message<Operation>> data_to_sync = new ConcurrentQueue<>();

    /**
     * Parameterized constructor that initializes an instance of AsynchronousServerProcess.
     *
     * @param p The port where the server will operate.
     * @param net An array containing ints representing the ports that the servers are running.
     * */
    @SuppressWarnings("unchecked")
    AsynchronousServerProcess(int p, int[] net){
        this.port = p;
        this.network = net;

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncProcess", Address.from(this.port), new MessagingConfig());
        this.mms.start().
                thenRun(() -> System.out.println("["+this.port+"]: Messaging Service Started"));

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

        this.sent_messages = new ArrayList<>();
        this.journal = new Journal(this.port+"_middleware", this.message_serializer);

        List<Object> msgs = this.journal.getObjectsLog();
        if(msgs.size() == 0) {
            // if log has nothing then it's the first time executing
            this.cd = new CausalDelivery(p, net, this, null);
            this.crash_recovery = false;
            this.last_clock = null;
            this.messages_to_recover = null;
        } else{
            // if log has something, then it's restarting
            // the last message that is kept is one sent by us
            // auxiliary list to keep messages received after a message sent by us, until another message sent by us is received
            List<Message<Operation>> aux = new ArrayList<>();
            for (Object o : msgs) {
                Message<Operation> msg = (Message<Operation>) o;
                aux.add(msg);
                if (msg.port == this.port) {
                    this.sent_messages.add(msg);
                    for (Message<Operation> operationMessage : aux) {
                        this.servers_messages.add(operationMessage.getObject());
                    }
                    aux = new ArrayList<>();
                }
            }
            if(this.sent_messages.size() == 0){
                this.last_clock = new HashMap<>();
                for (int value : this.network)
                    this.last_clock.put(value, 0);
            } else this.last_clock = this.sent_messages.get(this.sent_messages.size()-1).sender_vector_clock;

            this.messages_to_recover = new HashMap<>();
            this.cd = new CausalDelivery(p,net,this, this.last_clock);
            this.crash_recovery = true;
        }

        this.num_server_recovered = 0;

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

        // if this started with a crash, we must report
        if(this.crash_recovery) {
            System.out.println("["+this.port+"]: CRASHED!! Initialize recovery ...");
            this.sendCrashToServers(this.last_clock);
        }

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
            // WE NEED TO RESPOND SOMETHING TO CLIENT CASE ITS IN RECOVERY
        }, e);

        // handler for receiving data from other servers
        this.mms.registerHandler("DataToSync", (a,b) ->{
            System.out.println("["+this.port+"]: Received DATA TO SYNC from server " + a.port());
            Message<Operation> msg = this.message_serializer.decode(b);

            if(!this.crash_recovery) {
                // if server is not on crash recovery
                this.data_to_sync.add(msg);
            } else{
                // if server is on crash recovery
                if(this.messages_to_recover.containsKey(a.port())){
                    this.data_to_sync.add(msg);
                } else{
                    this.delayed_messages.add(msg);
                }
            }
        }, e);

        // handler for recovering data from other servers
        this.mms.registerHandler("DataToRecover", (a,b) ->{
            System.out.println("["+this.port+"]: Received DATA TO RECOVER from server " + a.port());
            Message<Operation> msg = this.message_serializer.decode(b);
            this.data_to_sync.add(msg);
        }, e);

        // handler for recovering data from other servers
        this.mms.registerHandler("MessagesToRecover", (a,b)->{
            try{
                System.out.println("["+this.port+"]: Received number of MESSAGES TO RECOVER from server " + a.port());
                Serializer s = new SerializerBuilder().build();
                int number = s.decode(b);
    
                this.messages_to_recover.put(a.port(),number);
    
                for(int i = 0 ; i < this.delayed_messages.size() ; i++){
                    Message<Operation> msg = this.delayed_messages.get(i);
                    if(msg.port == a.port() ){
                        if(msg.sender_vector_clock.get(a.port()) > number) {
                            this.data_to_sync.add(msg);
                        }
                        this.delayed_messages.remove(i);
                    }
                }
            }
            catch(Exception error){
                error.printStackTrace();
            }
        }, e);

        // handler for receiving crashes from other servers
        this.mms.registerHandler("Crash", (a,b) ->{
            System.out.println("["+this.port+"]: Received CRASH CLOCK MAP from server " + a.port());
            Serializer s = new SerializerBuilder().build();
            Map<Integer,Integer> clock = s.decode(b);

            // spawns a thread to do this computation asynchronously
            CompletableFuture.runAsync(() -> this.recoverLogMessages(clock, a.port()));
        }, e);

        // handler for receiving crashes from other servers
        this.mms.registerHandler("FinishedRecovery", (a,b) ->{
            System.out.println("["+this.port+"]: Received FinishedRecovery from server " + a.port());
            this.num_server_recovered++;
            // check if process has terminated
            if(this.num_server_recovered == (this.network.length-1))
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
        this.journal.writeObject(msg);
        this.sent_messages.add(msg);
        for (int server_port : this.network)
            if(server_port!=this.port)
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
        return this.clients_messages.poll();
    }

    /**
     *  Method that puts a tuple in the client messages queue to be consumed
     *
     * @param client_port port from the source client
     * @param op Message that will be added to the list
     * */
    private void addClientMessage(int client_port, Operation op){
        this.clients_messages.add(new Tuple<>(client_port, op));
    }

    /**
     * Method that consumes the head of the server messages queue when its available.
     *
     * @return Operation Operation from client on other server.
     * */
    Operation getServerMessage(){
        return this.servers_messages.poll();
    }

    /**
     *  Method that puts an operation in the server messages queue to be consumed.
     *
     * @param op Operation that will be added to the list.
     * */
    void addServerMessage(Message<Operation> op){
        this.journal.writeObject(op);
        this.servers_messages.add(op.getObject());
    }

    /**
     * Method that consumes the head of the messages to sync queue when its available.
     *
     * @return Tuple<Integer, Operation> Tuple with port and operation.
     * */
    Message<Operation> getDataToSync(){
        return this.data_to_sync.poll();
    }

    /**
     * Method recovers messages and sends'em to the crashed server.
     *
     * @param clock Vector Clock that has the last recorded clock.
     * @param server_port Port of the crashed server.
     * */
    private void recoverLogMessages(Map<Integer,Integer> clock, int server_port){
        int begin = clock.get(this.port);
        int end = this.sent_messages.size();

        Serializer s = new SerializerBuilder().build();
        this.mms.sendAsync(Address.from(server_port), "MessagesToRecover", s.encode(end));

        for(int i = begin ; i < end ; i++)
            this.mms.sendAsync(Address.from(server_port), "DataToRecover", this.message_serializer.encode(this.sent_messages.get(i)));

        this.mms.sendAsync(Address.from(server_port), "FinishedRecovery", s.encode("FinishedRecovery"));
    }
}
