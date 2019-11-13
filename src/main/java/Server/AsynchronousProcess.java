package Server;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AsynchronousProcess extends Thread{
    private int port;
    private ManagedMessagingService mms;

    /**
     * Parameterized constructor that initializes an instance of AsynchronousProcess.
     * */
    public AsynchronousProcess(int p){
        this.port = p;
    }

    /**
     * This method is the thread's work while it's running.
     * Initializes the Messaging Service and registers the fundamental handlers.
     * */
    @Override
    public void run() {
        // Thread pool for execution
        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncProcesses", Address.from(this.port), new MessagingConfig());
        this.mms.start().
                thenRun(() -> {
                    System.out.println("["+this.port+"]: Messaging Service Started");
                });

    }
}
