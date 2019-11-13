package Server;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynchronousProcess extends Thread{
    private int port;
    private ManagedMessagingService mms;
    private int[] network;

    /**
     * Parameterized constructor that initializes an instance of AsynchronousProcess.
     * */
    public AsynchronousProcess(int p){
        this.port = p;
        this.network = new int[]{ 12345, 23456, 34567, 45678, 56789 };
    }

    /**
     * This method is the thread's work while it's running.
     * Initializes the Messaging Service and registers the fundamental handlers.
     * */
    @Override
    public void run() {
        // Thread pool for execution
        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

        Serializer s = new SerializerBuilder().build();

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncProcesses", Address.from(this.port), new MessagingConfig());
        this.mms.start().
                thenRun(() -> {
                    System.out.println("["+this.port+"]: Messaging Service Started");
                });



        // testing connection, not functionality
        Runnable task = () -> {
            for (int i = 0; i < this.network.length; i++) {
                this.mms.sendAsync(Address.from(this.network[i]), "HELLO", s.encode("HELLO"));
            }
        };

        ScheduledFuture<?> scheduledFuture = e.scheduleAtFixedRate(task,1000,  1000,TimeUnit.MILLISECONDS);

        AtomicInteger netLen = new AtomicInteger(5);
        List<Integer> servers = new ArrayList<>();
        this.mms.registerHandler("HELLO",(a,b) -> {

            System.out.println("["+this.port+"]: Message Received from "+a.port());

            if(!servers.contains(a.port())){
                netLen.getAndDecrement();
                servers.add(a.port());
                if(netLen.get() == 0){
                    scheduledFuture.cancel(false);
                    this.mms.stop()
                            .thenRun(() -> {
                                System.out.println("["+this.port+"]: Messaging Service Stoped");
                                System.exit(0);
                            });
                }
            }
        }, e);

    }
}
