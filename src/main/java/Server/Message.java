package Server;

import java.util.Map;

/**
 * This class is intended to represent the messages exchanged between servers.
 * */
public class Message<T> {
    /** Sender port */
    public int port;
    /** Represents any object which is intended to be exchanged among servers */
    public final T object;
    /** An event vector --> Map<server port, event counter> */
    public Map<Integer,Integer> sender_vector_clock;

    /**
     * Constructor that initializes an instance of Message.
     * */
    public Message(int p, T obj, Map<Integer,Integer> clock){
        this.port = p;
        this.object = obj;
        this.sender_vector_clock = clock;
    }
}
