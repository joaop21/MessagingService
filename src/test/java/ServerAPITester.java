import Middleware.ServerMiddlewareAPI;
import Middleware.Tuple;
import Operations.Operation;
import Operations.OperationType;
import Operations.Post.Post;
import Operations.Post.PostType;
import Operations.Reply.Confirm;
import Operations.Reply.Response;
import Operations.Reply.ResponseMessages;
import Operations.Reply.ResponseTopics;
import Operations.Request.Request;

import java.util.LinkedList;

public class ServerAPITester {

    public static void main(String[] args) throws InterruptedException {
        int port = Integer.parseInt(args[0]);

        ServerMiddlewareAPI api = new ServerMiddlewareAPI(port);

        // client handler
        Thread c = new Thread(new ClientsHandler(api));
        Thread s = new Thread(new ServersHandler(api));

        c.start();
        s.start();

        c.join();
        s.join();
    }
}

class ClientsHandler extends Thread{
    private ServerMiddlewareAPI api;

    ClientsHandler(ServerMiddlewareAPI api){this.api = api;}

    @Override
    public void run() {
        while(true){
            Tuple<Integer, Operation> t = this.api.getNextClientMessage();

            if(t.getSecond().getType() == OperationType.REQUEST){
                Request r = (Request) t.getSecond().getOp();
                switch (r.getType()){
                    case MESSAGES:
                        Response resp1 = new Response(new ResponseMessages(new LinkedList<>()));
                        this.api.sendMessageToClient(t.getFirst(),resp1);
                        break;
                    case TOPICS:
                        Response resp2 = new Response(new ResponseTopics(new LinkedList<>()));
                        this.api.sendMessageToClient(t.getFirst(),resp2);
                        break;
                }
            } else{
                if(t.getSecond().getType() == OperationType.POST){
                    Post p = (Post) t.getSecond().getOp();
                    Response resp = new Response(new Confirm(true));
                    this.api.sendMessageToClient(t.getFirst(),resp);
                }
            }
        }
    }
}

class ServersHandler extends Thread{
    private ServerMiddlewareAPI api;

    ServersHandler(ServerMiddlewareAPI api){this.api = api;}

    @Override
    public void run() {
        while(true){
            Operation op = this.api.getNextServerMessage();
        }
    }
}
