import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Application.Journal;
import Application.Topic;
import Middleware.Message;
import Operations.Operation;
import Operations.OperationType;
import Operations.Post.Post;
import Operations.Post.PostLogin;
import Operations.Post.PostMessage;
import Operations.Post.PostTopics;
import Operations.Post.PostType;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

public class JournalTester {

    static void test1(){
        String fileName = "TesterLog";

        Map<Integer,Integer> vector_clock = new HashMap<>();
        vector_clock.put(12345, 5);
        vector_clock.put(23456, 2);
        vector_clock.put(34567, 3);
        vector_clock.put(45678, 6);

        Serializer serializer = new SerializerBuilder()
                .build();

        Journal j = new Journal(fileName,serializer);

        Map<Integer,Integer> obj = (Map<Integer, Integer>) j.getLastObject();
        if(obj == null)
            System.out.println("There's nothing in the log file.");
        else System.out.println("Last register in Log: "+obj.toString());

        j.writeObject(vector_clock);
        System.out.println("Object written to log.");
    }

    static void test2(){
        String fileName = "12345_middleware";
        Serializer message_serializer = new SerializerBuilder()
                .withTypes(Message.class, Operation.class, OperationType.class, Post.class, PostMessage.class,
                        PostTopics.class, PostLogin.class, PostType.class, Application.Post.class, Topic.class)
                .build();
        Journal journal = new Journal(fileName, message_serializer);

        List<Object> msgs = journal.getObjectsLog();

        for (Object o : msgs){
            Message<Operation> msg = (Message<Operation>) o;
            switch(msg.getObject().getType()){
                case POST:
                    Post p = (Post) msg.getObject().getOp();
                    if(p.getPostType() == PostType.MESSAGE) {
                        PostMessage pm = (PostMessage) p.getPost();
                        System.out.println(pm.getPost().toString());
                    }
            }
        }
    }

    public static void main(String[] args) {
        test2();
    }
}
