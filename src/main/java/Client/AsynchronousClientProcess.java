package Client;

import Operations.Reply.Response;
import Operations.Request.Request;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsynchronousClientProcess extends Thread{
    /** Address from server to contact*/
    private Address server;

    private ManagedMessagingService mms;
    private Serializer response_serializer;
    private Serializer request_serializer;

    /** Responses from servers **/
    private Queue<Response> responses = new LinkedList<>();

    /**
     * Parameterized constructor that initializes an instance of AsynchronousClientProcess.
     *
     * @param port The port where the client will operate.
     * @param server The port where the server will operate.
     * */
    AsynchronousClientProcess(int port, int server){
        this.server = Address.from(server);

        // Initializes Atomix messaging service
        this.mms = new NettyMessagingService("AsyncClientProcess", Address.from(port), new MessagingConfig());

        // Initializes a Serializer capable of encode and decode Responses
        this.response_serializer = new SerializerBuilder()
                .addType(Response.class)
                .build();

        // Initializes a Serializer capable of encode and decode Requests
        this.request_serializer = new SerializerBuilder()
                .addType(Request.class)
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

        // Handler for receive responses from server
        this.mms.registerHandler("Response", (a,b) -> {
            Response resp = this.response_serializer.decode(b);
            addResponse(resp);
        }, e);

        this.mms.start();
    }

    /**
     * Method that sends a Request to server.
     *
     * @param r The request to be sent.
     * */
    void sendRequest(Request r){
        this.mms.sendAsync(this.server,"Request",this.request_serializer.encode(r));
    }

    /**
     * Method used by the main thread for adding a response to the queue.
     *
     * @param r The response to be saved in the queue.
     * */
    private void addResponse(Response r){
        this.responses.add(r);
        notify();
    }

    /**
     * Method that gets the next response from server.
     *
     * @return Response The next response to be treated.
     * */
    Response getResponse(){
        while(this.responses.size() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.responses.poll();
    }
}
