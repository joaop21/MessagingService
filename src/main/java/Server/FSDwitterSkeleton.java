package Server;

import java.util.List;

import Application.*;
import Middleware.*;

public class FSDwitterSkeleton implements FSDwitter {
    private FSDwitterImpl fsdwitter;
    private ServerMiddlewareAPI smi;
    private ClientMessageHandler cmh;
    private ServerMessageHandler smh;

    FSDwitterSkeleton(int port) {
        this.fsdwitter = new FSDwitterImpl();
        this.smi = new ServerMiddlewareAPI(port);
        this.cmh = new ClientMessageHandler(this, smi);
        this.smh = new ServerMessageHandler(this, smi);
    }

    @Override
    public List<Post> get_10_recent_posts(String username) {
        return fsdwitter.get_10_recent_posts(username);
    }

    @Override
    public List<Topic> get_topics(String username) {
        return fsdwitter.get_topics(username);
    }

    @Override
    public boolean make_post(Post p) {
        return fsdwitter.make_post(p);
    }

    @Override
    public boolean set_topics(String username, List<Topic> topics) {
        return fsdwitter.set_topics(username, topics);
    }

    @Override
    public boolean is_auth(String username, String password) {
        return fsdwitter.is_auth(username, password);
    }

}