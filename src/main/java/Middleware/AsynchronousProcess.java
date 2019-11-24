package Middleware;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.Arrays;
import java.util.concurrent.*;

class AsynchronousProcess extends Thread{
    private int port;
    private int[] network;

    private CausalDelivery cd;
    private boolean is_server;
    private ServerMiddleware serv_midd;
    private ClientMiddleware cli_midd;

    private ManagedMessagingService mms;
    private Serializer serializer;

    /**
     * Parameterized constructor that initializes an instance of AsynchronousProcess.
     *
     * @param p The port where the server will operate.
     * @param mid An instance of ServerMiddleware object.
     * @param c An instance of the CausalDelivery object.
     * @param net An array containing ints representing the ports that the servers are running.
     * */
    AsynchronousProcess(int p, ServerMiddleware mid, CausalDelivery c, Object[] net){
        this.port = p;
        this.cd = c;
        this.is_server = true;
        this.serv_midd = mid;
        this.cli_midd = null;
        this.network = Arrays.stream(net).mapToInt(o -> (int) o).toArray();

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncProcesses", Address.from(this.port), new MessagingConfig());
        this.mms.start().
                thenRun(() -> {
                    System.out.println("["+this.port+"]: Messaging Service Started");
                });

        // Initializes a Serializer capable of encode and decode Messages
        this.serializer = new SerializerBuilder()
                .addType(Message.class)
                .build();
    }

    /**
     * Parameterized constructor that initializes an instance of AsynchronousProcess.
     *
     * @param p The port where the server will operate.
     * @param mid An instance of ServerMiddleware object.
     * @param net An array containing ints representing the ports that the servers are running.
     * */
    AsynchronousProcess(int p, ClientMiddleware mid, int[] net){
        this.port = p;
        this.cd = null;
        this.is_server = false;
        this.serv_midd = null;
        this.cli_midd = null;
        this.network = net;

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncProcesses", Address.from(this.port), new MessagingConfig());
        this.mms.start().
                thenRun(() -> {
                    System.out.println("["+this.port+"]: Messaging Service Started");
                });

        // Initializes a Serializer capable of encode and decode Messages
        this.serializer = new SerializerBuilder()
                .addType(Message.class)
                .build();
    }

    /**
     * This method is the thread work while it's running.
     * Registers the fundamental handlers for events.
     * */
    @Override
    public void run() {
        // Thread pool for execution
        ExecutorService e = Executors.newFixedThreadPool(1);

        // handler for an event "ServerToServer"
        this.mms.registerHandler("ServerToServer",(a,b) -> {
            if(is_server)
                this.cd.receive(serializer.decode(b));
        },e);

        // handler for an event "ClientToServer"
        this.mms.registerHandler("ClientToServer",(a,b) -> {
            if(is_server)
                this.serv_midd.addClientMessage(a.port(),serializer.decode(b));
        },e);

        // handler for an event "ServerToClient"
        this.mms.registerHandler("ServerToClient",(a,b) -> {
            if(!is_server)
                this.cli_midd.addMessage(serializer.decode(b));
        },e);
    }

    /**
     * Sends Message to all adjacent servers.
     *
     * @param msg The message to be sent.
     *
     * */
    void sendMessageToServers(Message msg){
        if(is_server)
            for (int value : this.network)
                if(value!=this.port)
                    this.mms.sendAsync(Address.from(value), "ServerToServer", this.serializer.encode(msg));
    }

    /**
     * Sends Message to a specific server..
     *
     * @param msg The message to be sent.
     *
     * */
    void sendMessageToServer(int p, Message msg){
        if(!is_server)
            this.mms.sendAsync(Address.from(p), "ClientToServer", this.serializer.encode(msg));
    }

    /**
     * Sends Message to a specific client.
     *
     * @param client_port The port where the client is.
     * @param msg The message to be sent.
     *
     * */
    void sendMessageToClient(int client_port, Message msg){
        if(is_server)
            this.mms.sendAsync(Address.from(client_port), "ServerToClient", this.serializer.encode(msg));
    }
}
