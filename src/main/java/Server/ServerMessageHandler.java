package Server;

import Middleware.ServerMiddlewareAPI;
import Operations.Operation;
import Operations.Post.Post;
import Operations.Post.PostMessage;
import Operations.Post.PostTopics;

public class ServerMessageHandler implements Runnable {
    private FSDwitterSkeleton skeleton;
    private ServerMiddlewareAPI smi;

    public ServerMessageHandler(FSDwitterSkeleton skeleton, ServerMiddlewareAPI smi){
        this.skeleton = skeleton;
        this.smi = smi;
    }

    @Override
    public void run() {
        while(true){
            Operation op = smi.getNextServerMessage();
            switch(op.getType()){
                case POST:
                    Post post = (Post) op.getOp();
                    switch(post.getPostType()){
                        case MESSAGE:
                            PostMessage postMsg = (PostMessage) post.getPost();
                            skeleton.make_post(postMsg.getPost());
                            break;
                        case TOPICS:
                            PostTopics postTopics = (PostTopics) post.getPost();
                            skeleton.set_topics(postTopics.getUsername(), postTopics.getTopics());
                            break;
                        default:
                            // wont receive other post types
                            System.out.print("Post type not supported");
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