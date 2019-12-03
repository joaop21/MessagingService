package Server;

import Middleware.*;
import Operations.Operation;
import Operations.Post.*;
import Operations.Reply.*;
import Operations.Request.*;

public class ClientMessageHandler implements Runnable {
    private FSDwitterSkeleton skeleton;
    private ServerMiddlewareAPI smi;

    public ClientMessageHandler(FSDwitterSkeleton skeleton, ServerMiddlewareAPI smi){
        this.skeleton = skeleton;
        this.smi = smi;
    }

    @Override
    public void run() {
        while(true){
            Tuple<Integer,Operation> tuple = smi.getNextClientMessage();
            int client_port = tuple.getFirst();
            Operation op = tuple.getSecond();
            switch(op.getType()){
                case REQUEST: 
                    Request req = (Request) op.getOp();
                    switch(req.getType()){
                        case MESSAGES: 
                            RequestMessages reqMsgs = (RequestMessages) req.getObj();
                            Response responseMsgs = new Response(
                                new ResponseMessages(
                                    skeleton.get_10_recent_posts(reqMsgs.getUsername())
                                )
                            );
                            smi.sendMessageToClient(client_port, responseMsgs);
                            break;

                        case TOPICS:
                            RequestTopics reqTopics = (RequestTopics) req.getObj();
                            Response responseTopics = new Response(
                                new ResponseTopics(
                                    skeleton.get_topics(reqTopics.getUsername())
                                )
                            );
                            smi.sendMessageToClient(client_port, responseTopics);                    
                            break;
                    }
                    break;
                case POST:
                    Post post = (Post) op.getOp();
                    switch(post.getPostType()){
                        case LOGIN:
                            PostLogin postLogin = (PostLogin) post.getPost();
                            Response responseLogin = new Response(
                                new Confirm(
                                    skeleton.is_auth(postLogin.getUsername(), postLogin.getPassword())
                                )
                            );
                            smi.sendMessageToClient(client_port, responseLogin);
                            break;
                        case MESSAGE:
                            PostMessage postMessage = (PostMessage) post.getPost();
                            Response responseMessage = new Response(
                                new Confirm(
                                    skeleton.make_post(postMessage.getPost())
                                )
                            );
                            smi.sendMessageToClient(client_port, responseMessage);
                            break;
                        case TOPICS:
                            PostTopics postTopics = (PostTopics) post.getPost();
                            Response responseTopics = new Response(
                                new Confirm(
                                    skeleton.set_topics(postTopics.getUsername(), postTopics.getTopics())
                                )
                            );
                            smi.sendMessageToClient(client_port, responseTopics);
                            break;
                    }
                    break;
                default:
                    // wont receive other message types
                    System.out.println("Message type not supported"); 
                    break;
            }
        }
    }
}