package Application;

import java.util.*;

public class BoundedQueue<T> {
    private final int max;
    private int size;
    private Queue<T> queue;

    /**
     * Parameterized constructor to create an instance of this object.
     *
     * @param capacity Max Size of the queue.
     * */
    BoundedQueue(int capacity){
        this.max = capacity;
        this.size = 0;
        this.queue = new LinkedList<>();
    }

    /**
     *  Method that adds an object to the queue and removes the oldest one in order to
     *      maintain the initial capacity.
     *
     * @param obj Object to be inserted in the queue.
     * */
    synchronized void add(T obj){
        this.queue.add(obj);
        this.size++;
        if(this.size > this.max) {
            this.queue.remove();
            this.size--;
        }
    }

    /**
     * Method that returns all the elements in the queue
     *
     * @return List<T> List with the objects in the queue
     * */
    synchronized List<T> get(){
        return (List<T>) Arrays.asList(this.queue.toArray());
    }


}
