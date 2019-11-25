package Client;

import Operations.Reply.Response;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class ClientMiddlewareAPI {
    /** Responses from servers **/
    private Queue<Response> responses = new LinkedList<>();
    private int server_to_contact;
    private AsynchronousClientProcess asp;

    ClientMiddlewareAPI(int port){
        int[] network = new int[]{12345, 23456, 34567, 45678, 56789};

        Random rand = new Random();
        this.server_to_contact = network[rand.nextInt(5)];
    }
}
