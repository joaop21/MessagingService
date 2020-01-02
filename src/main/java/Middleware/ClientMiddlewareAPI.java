package Middleware;

import Configuration.Config;
import Operations.Post.Post;
import Operations.Reply.Response;
import Operations.Request.Request;

import java.io.IOException;
import java.util.Random;

public class ClientMiddlewareAPI {
    private AsynchronousClientProcess ascp;

    /**
     * Parameterized constructor that initializes an instance of ClientMiddlewareAPI.
     *
     * @param port The port where the client will operate.
     * */
    public ClientMiddlewareAPI(int port){
        try {
            Config conf = Config.loadConfig();
            int[] network = conf.getNetwork();

            // randomly chooses a server to contact
            Random rand = new Random();
            int server_to_contact = network[rand.nextInt(5)];

            this.ascp = new AsynchronousClientProcess(port, server_to_contact);
            this.ascp.start();
        } catch (IOException e){
            e.printStackTrace();
        }
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
     * @param request Request to send.
     * */
    public void sendRequest(Request request){
        this.ascp.sendRequest(request);
    }

    /**
     * Method that sends a post to server.
     *
     * @param post Post Operation.
     * */
    public void sendPost(Post post){
        this.ascp.sendRequest(post);
    }
}
