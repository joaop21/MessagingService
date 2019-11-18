package Server.Middleware;

import java.util.Map;

/**
 * This class is intended to represent the messages exchanged between servers.
 * */
class Message<T> {
    /** Sender port */
    int port;
    /** Represents any object which is intended to be exchanged among servers */
    private final T object;
    /** An event vector --> Map<server port, event counter> */
    Map<Integer,Integer> sender_vector_clock;

    /**
     * Constructor that initializes an instance of Message.
     * */
    public Message(int p, T obj, Map<Integer,Integer> clock){
        this.port = p;
        this.object = obj;
        this.sender_vector_clock = clock;
    }

    /**
     * Getter for instance variable Object.
     *
     * @return T Object that is passed on the Message.
     * */
    public T getObject(){ return this.object;}
}
