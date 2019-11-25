package Client;

import Operations.*;
import util.FSDwitter;
import util.Post;
import util.Topic;

import java.util.List;

public class FSDwitterStub implements FSDwitter {
    @Override
    public List<Post> get_10_recent_posts(String username) {
        RequestMessages rms = new RequestMessages(username);

        // enviar para middleware

        // receber resposta

        return null;
    }

    @Override
    public List<Topic> get_topics(String username) {
        RequestTopics rts = new RequestTopics(username);

        // enviar para middleware

        // receber resposta

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
