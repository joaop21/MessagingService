package Client;

import java.util.List;

import Application.FSDwitter;
import Application.Journal;
import Application.Post;
import Application.Topic;
import Middleware.ClientMiddlewareAPI;
import Operations.Post.PostLogin;
import Operations.Post.PostMessage;
import Operations.Post.PostTopics;
import Operations.Reply.Confirm;
import Operations.Reply.Response;
import Operations.Reply.ResponseMessages;
import Operations.Reply.ResponseType;
import Operations.Request.Request;
import Operations.Request.RequestMessages;
import io.atomix.utils.serializer.SerializerBuilder;

public class FSDwitterStub implements FSDwitter {
    private ClientMiddlewareAPI cma;
    private Journal journal;

    FSDwitterStub(int port){
        this.cma = new ClientMiddlewareAPI(port);
        this.journal = null;
    }

    @Override
    public List<Post> get_10_recent_posts(String username) {
        // sends to middleware
        this.cma.sendRequest(new Request(new RequestMessages(username)));

        // receives from middleware
        Response resp = this.cma.getResponse();
        if(resp.getType() == ResponseType.MESSAGES){
            ResponseMessages rms = (ResponseMessages) resp.getObj();
            return rms.getPosts();
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Topic> get_topics(String username) {
        // with request to server:
        
        // // sends to middleware
        // this.cma.sendRequest(new Request(new RequestTopics(username)));

        // // receives from middleware
        // Response resp = this.cma.getResponse();
        // if(resp.getType() == ResponseType.TOPICS){
        //     ResponseTopics rts = (ResponseTopics) resp.getObj();
        //     return rts.getTopics();
        // }

        // without request to server (using the journal):
        return (List<Topic>) this.journal.getLastObject();
    }

    @Override
    public boolean make_post(Post p) {
        PostMessage pm = new PostMessage(p);

        // sends to middleware
        this.cma.sendPost(new Operations.Post.Post(pm));

        // receives from middleware
        Response resp = this.cma.getResponse();
        if(resp.getType() == ResponseType.CONFIRM){
            Confirm cnf = (Confirm) resp.getObj();
            return cnf.getStatus();
        }

        return false;
    }

    @Override
    public boolean set_topics(String username, List<Topic> topics) {
        PostTopics pts = new PostTopics(username,topics);

        // sends to middleware
        this.cma.sendPost(new Operations.Post.Post(pts));

        // receives from middleware
        Response resp = this.cma.getResponse();
        if(resp.getType() == ResponseType.CONFIRM){
            Confirm cnf = (Confirm) resp.getObj();
            boolean status = cnf.getStatus();
            if (status)
                journal.writeObject(topics);
            return status;
        }

        return false;
    }

    @Override
    public boolean is_auth(String username, String password) {
        PostLogin pl = new PostLogin(username, password);

        // sends to middleware
        this.cma.sendPost(new Operations.Post.Post(pl));

        // receives from middleware
        Response resp = this.cma.getResponse();
        if(resp.getType() == ResponseType.CONFIRM){
            Confirm cnf = (Confirm) resp.getObj();
            boolean status = cnf.getStatus();
            if (status)
                this.journal = new Journal(username, new SerializerBuilder().withTypes(Topic.class).build());
            return status;
        }

        return false;
    }
}
