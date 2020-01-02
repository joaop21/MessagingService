package Middleware;

import java.io.IOException;

import Configuration.Config;
import Operations.Operation;
import Operations.Reply.Response;

public class ServerMiddlewareAPI {
    private AsynchronousServerProcess assp;

    public ServerMiddlewareAPI(int port){
        try {
            Config conf = Config.loadConfig();

            this.assp = new AsynchronousServerProcess(port,conf.getNetwork());

            new Thread(this.assp).start();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Method that returns the next received message from a client.
     *
     * @return Tuple<Integer, Operation> Tuple with port and operation.
     * */
    public Tuple<Integer, Operation> getNextClientMessage(){
        return this.assp.getClientMessage();
    }

    /**
     * Method that returns the next received message from a server.
     *
     * @return Tuple<Integer, Operation> Tuple with port and operation.
     * */
    public Operation getNextServerMessage(){
        return this.assp.getServerMessage();
    }

    /**
     * Method that sends a response to a client.
     *
     * @param client_port Port where the client is.
     * @param resp Response message sent to client.
     * */
    public void sendMessageToClient(int client_port, Response resp){
        this.assp.sendMessageToClient(client_port,resp);
    }

}
