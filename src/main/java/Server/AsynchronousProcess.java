package Server;

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
    private CausalDelivery cd;
    private int[] network;
    private ManagedMessagingService mms;
    private Serializer serializer;

    /**
     * Parameterized constructor that initializes an instance of AsynchronousProcess.
     *
     * @param p The port where the server will operate.
     * @param c An instance of the CausalDelivery object.
     * @param net An array containing ints representing the ports that the servers are running.
     * */
    AsynchronousProcess(int p, CausalDelivery c, Object[] net){
        this.port = p;
        this.cd = c;
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
     * This method is the thread work while it's running.
     * Registers the fundamental handlers for events.
     * */
    @Override
    public void run() {
        // Thread pool for execution
        ExecutorService e = Executors.newFixedThreadPool(1);

        // handler for an event "Message"
        this.mms.registerHandler("Message",(a,b) -> {

        },e);
    }

    /**
     * Sends Message to all adjacent servers.
     *
     * @param msg The message to be sent.
     *
     * */
    public void sendMessages(Message msg){
        for(int i = 0; i < this.network.length; i++){
            this.mms.sendAsync(Address.from(this.network[i]),"Message", this.serializer.encode(msg));
        }
    }
}
