package Client;

import Middleware.ClientMiddlewareAPI;
import Operations.Post.PostLogin;
import Operations.Post.PostMessage;
import Operations.Post.PostTopics;
import Operations.Reply.Response;
import Operations.Reply.ResponseType;
import Operations.Request.RequestMessages;
import Operations.Request.RequestTopics;
import Operations.Request.RequestType;
import util.FSDwitter;
import util.Post;
import util.Topic;

import java.util.List;

public class FSDwitterStub implements FSDwitter {
    private ClientMiddlewareAPI cma;

    FSDwitterStub(int port){
        this.cma = new ClientMiddlewareAPI(port);
    }

    @Override
    public List<Post> get_10_recent_posts(String username) {
        RequestMessages rms = new RequestMessages(username);

        // sends to middleware
        this.cma.sendRequest(RequestType.MESSAGES,rms);

        // receives from middleware
        Response resp = this.cma.getResponse();
        if(resp.getType() == ResponseType.MESSAGES){
            return resp.getRms().getPosts();
        }

        return null;
    }

    @Override
    public List<Topic> get_topics(String username) {
        RequestTopics rts = new RequestTopics(username);

        // sends to middleware
        this.cma.sendRequest(RequestType.TOPICS, rts);

        // receives from middleware
        Response resp = this.cma.getResponse();
        if(resp.getType() == ResponseType.TOPICS){
            return resp.getRts().getTopics();
        }

        return null;
    }

    @Override
    public boolean make_post(Post p) {
        PostMessage pm = new PostMessage(p);

        // enviar para middleware

        // receber resposta

        return false;
    }

    @Override
    public boolean set_topics(String username, List<Topic> topics) {
        PostTopics pts = new PostTopics(username,topics);

        // enviar para middleware

        // receber resposta

        return false;
    }

    @Override
    public boolean is_auth(String username, String password) {
        PostLogin pl = new PostLogin(username, password);

        // enviar para middleware

        // receber resposta

        return false;
    }
}
