package Middleware;

import Operations.Reply.Response;
import Operations.Request.Request;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.Arrays;
import java.util.concurrent.*;

class AsynchronousServerProcess extends Thread{
    private int port;
    private int[] network;

    private CausalDelivery cd;

    private ManagedMessagingService mms;
    private Serializer response_serializer;
    private Serializer request_serializer;

    /**
     * Parameterized constructor that initializes an instance of AsynchronousServerProcess.
     *
     * @param p The port where the server will operate.
     * @param c An instance of the CausalDelivery object.
     * @param net An array containing ints representing the ports that the servers are running.
     * */
    AsynchronousServerProcess(int p, CausalDelivery c, Object[] net){
        this.port = p;
        this.cd = c;
        this.network = Arrays.stream(net).mapToInt(o -> (int) o).toArray();

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncServerProcess", Address.from(this.port), new MessagingConfig());

        // Initializes Serializers capable of encode and decode Objects
        this.response_serializer = new SerializerBuilder().addType(Response.class).build();
        this.request_serializer = new SerializerBuilder().addType(Request.class).build();
    }

    /**
     * This method is the thread work while it's running.
     * Registers the fundamental handlers for events.
     * */
    @Override
    public void run() {
        // Thread pool for execution
        ExecutorService e = Executors.newFixedThreadPool(1);

        // handler for receiving requests
        // they're only sent from clients
        this.mms.registerHandler("Request", (a,b) ->{
            // deliver to class which has maps
        }, e);

        // handler for receiving posts
        // they're only sent from clients
        this.mms.registerHandler("Post", (a,b) ->{
            // test if is a login
            // true -> send to app
            // else -> send to app and other servers because state is beeing changed
        }, e);

        // handler for receiving data from other servers
        this.mms.registerHandler("DataSync", (a,b) ->{
            // deliver to causalDelivery
        }, e);

        this.mms.start().
                thenRun(() -> {
                    System.out.println("["+this.port+"]: Messaging Service Started");
                });
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
}
