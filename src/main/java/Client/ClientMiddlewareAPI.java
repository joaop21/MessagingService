package Client;

import Operations.Reply.Response;
import Operations.Request.Request;
import Operations.Request.RequestType;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class ClientMiddlewareAPI {
    private AsynchronousClientProcess ascp;

    /**
     * Parameterized constructor that initializes an instance of ClientMiddlewareAPI.
     *
     * @param port The port where the client will operate.
     * */
    public ClientMiddlewareAPI(int port){
        int[] network = new int[]{12345, 23456, 34567, 45678, 56789};

        // randomly chooses a server to contact
        Random rand = new Random();
        int server_to_contact = network[rand.nextInt(5)];

        this.ascp = new AsynchronousClientProcess(port, server_to_contact);
        this.ascp.start();
    }

    /**
     * Method that gets the next response from server.
     *
     * @return Response The next response to be treated.
     * */
    public Response getResponse(){
        return this.ascp.getResponse();
    }

    /**
     * Method that sends a Request to server.
     *
     * @param type The request type to be sent.
     * @param o Object that is some type of request.
     * */
    public void sendRequest(RequestType type, Object o){
        Request r = new Request(type,o);

        this.ascp.sendRequest(r);
    }
}
