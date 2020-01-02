import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Application.Post;
import Application.Topic;
import Middleware.ClientMiddlewareAPI;
import Operations.Post.PostLogin;
import Operations.Post.PostMessage;
import Operations.Post.PostTopics;
import Operations.Reply.Confirm;
import Operations.Reply.Response;
import Operations.Reply.ResponseMessages;
import Operations.Reply.ResponseTopics;
import Operations.Reply.ResponseType;
import Operations.Request.Request;
import Operations.Request.RequestMessages;
import Operations.Request.RequestTopics;

public class ClientAPITester extends Thread{
    private static boolean started, stopped;
    private static int n;
    private static long total;
    private static final long time = 30;

    @Override
    public void run() {
        // simulate
        Random rand = new Random();
        int port = rand.nextInt(65535-49152) + 49152;
        ClientMiddlewareAPI api = new ClientMiddlewareAPI(port);

        String username = "joao";
        String pass = "joaopass";
        String text = "Novo topico com algum texto " + port;
        List<Topic> topics = new ArrayList<>();
        topics.add(Topic.SPORTS);
        topics.add(Topic.NEWS);

        while(!terminated()) {
            int op = rand.nextInt(5);

            long before = System.nanoTime();

            switch (op) {
                case 0:
                    api.sendRequest(new Request(new RequestMessages(username)));
                    Response resp1 = api.getResponse();
                    if(resp1.getType() == ResponseType.MESSAGES){
                        ResponseMessages rms = (ResponseMessages) resp1.getObj();
                    }
                    break;
                case 1:
                    api.sendRequest(new Request(new RequestTopics(username)));
                    Response resp2 = api.getResponse();
                    if(resp2.getType() == ResponseType.TOPICS){
                        ResponseTopics rts = (ResponseTopics) resp2.getObj();
                    }
                    break;
                case 2:
                    PostMessage pm = new PostMessage(new Post(username,System.currentTimeMillis(),text,topics));
                    api.sendPost(new Operations.Post.Post(pm));
                    Response resp3 = api.getResponse();
                    if(resp3.getType() == ResponseType.CONFIRM){
                        Confirm cnf = (Confirm) resp3.getObj();
                    }
                    break;
                case 3:
                    PostTopics pts = new PostTopics(username,topics);
                    api.sendPost(new Operations.Post.Post(pts));
                    Response resp4 = api.getResponse();
                    if(resp4.getType() == ResponseType.CONFIRM){
                        Confirm cnf = (Confirm) resp4.getObj();
                    }
                    break;
                case 4:
                    PostLogin pl = new PostLogin(username, pass);
                    api.sendPost(new Operations.Post.Post(pl));
                    Response resp = api.getResponse();
                    if(resp.getType() == ResponseType.CONFIRM){
                        Confirm cnf = (Confirm) resp.getObj();
                    }
                    break;
            }

            long after = System.nanoTime();

            registerTime(after-before);
        }
    }

    public synchronized static void startBench() {
        started = true;

        System.out.println("Started!");
    }

    public synchronized static void stopBench() {
        stopped = true;

        // Response time
        System.out.println("Response Time = "+(total/(n*1e9d)));

        // Throughput
        System.out.println("Throughput = "+(n/((double)time)));
    }

    public synchronized static void registerTime(long tr) {
        if (started && !stopped) {
            n++;
            total += tr;
        }
    }

    public synchronized static boolean terminated() {
        return stopped;
    }

    public static void main(String[] args) throws Exception {
        ClientAPITester[] list = new ClientAPITester[20];

        // create instances of this object
        for(int i=0; i < list.length; i++)
            list[i] = new ClientAPITester();

        // Start testing threads
        for (ClientAPITester apiTester : list)
            apiTester.start();

        Thread.sleep(5000); // warm up

        startBench();

        Thread.sleep(time*1000);  // measures

        stopBench();

        // wait for threads to die
        for (ClientAPITester clientAPITester : list)
            clientAPITester.join();
    }
}
