package Middleware;

import Application.Topic;
import Operations.Operation;
import Operations.OperationType;
import Operations.Post.*;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.*;

class CausalDelivery extends Thread{
    private int port;
    private int event_counter = 0;
    private Map<Integer,Integer> local_vector_clock = new HashMap<>();
    private List<Message<Operation>> waiting_queue = new LinkedList<>();
    private AsynchronousServerProcess assp;

    /**
     * Parameterized constructor that initializes an instance of CausalDelivery.
     *
     * @param p The port where the server will operate.
     * */
    CausalDelivery(int p, int[] net, AsynchronousServerProcess assp, Map<Integer,Integer> clock){
        this.port = p;
        this.assp = assp;

        if(clock == null)
            for (int value : net)
                this.local_vector_clock.put(value, 0);
        else this.local_vector_clock = clock;

    }

    /**
     * This method is the thread work while it's running.
     * It's intended to keep causal delivery.
     * */
    @Override
    public void run() {
        while(true){
            Message<Operation> msgop = this.assp.getDataToSync();
            receive(msgop);
        }
    }

    /**
     * Method that is responsable for testing causal delivery rules and act depending
     *   on the situation.
     *
     * @param msg Message that contains info to analyse.
     * */
    synchronized void receive(Message<Operation> msg){

        if (firstCausalRule(msg)) {
            if(!secondCausalRule(msg)) {
                this.waiting_queue.add(msg);
                return;
            }
        } else {
            this.waiting_queue.add(msg);
            return;
        }

        // When passes the Causal tests, change the clock
        this.local_vector_clock.replace(msg.port, msg.sender_vector_clock.get(msg.port));

        // adds object passed to the queue in AsynServerProcess
        this.assp.addServerMessage(msg.getObject());

        // Makes the routine N times until the state get consistent
        // Could exist messages on the end of the queue that unlock messages on the first places
        // it's necessary run N time
        while(checkPendingMessages());
    }

    /**
     * Routine that checks if waiting messages are ready to be shown.
     * It has to check causal rules.
     * Returns true on the first message that passes the test.
     *
     * @return Boolean Value returned if there was/wasn't a message ready to be delivered.
     * */
    private boolean checkPendingMessages(){
        int tam = this.waiting_queue.size(); // calculated only once
        for(int i = 0 ; i < tam ; i++){
            Message<Operation> msg = this.waiting_queue.get(i);

            if(firstCausalRule(msg) && secondCausalRule(msg)){
                // When passes the Causal tests, change the clock
                this.local_vector_clock.replace(msg.port, msg.sender_vector_clock.get(msg.port));
                // adds object passed to the queue in AsynServerProcess
                this.assp.addServerMessage(msg.getObject());
                // removes from queue
                this.waiting_queue.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * This method tests the first causal rule:
     *      l[i]+1=r[i]
     *
     * @param msg Message that contains info to analyse.
     *
     * @return boolean The boolean value represents the test result.
     * */
    private boolean firstCausalRule(Message<Operation> msg){
        int local_value = this.local_vector_clock.get(msg.port);
        int sender_value = msg.sender_vector_clock.get(msg.port);
        return (sender_value - local_value) == 1;
    }

    /**
     * This method tests the second causal rule:
     *      For all j!=i: r[j]<=l[j]
     *
     * @param msg Message that contains info to analyse.
     *
     * @return boolean The boolean value represents the test result.
     * */
    private boolean secondCausalRule(Message<Operation> msg){
        Set<Map.Entry<Integer,Integer>> k_values = msg.sender_vector_clock.entrySet();

        for (Map.Entry<Integer, Integer> entry : k_values) {
            // case is not the sender port && the second rule fails
            if ((msg.port != entry.getKey()) && (this.local_vector_clock.get(entry.getKey()) < entry.getValue()))
                return false;
        }
        return true;
    }

    /**
     * Method that increments the event counter and sends the massage to other servers.
     *
     * @param op Operation to be passed to the other servers.
     * */
    synchronized void sendMessageToServers(Operation op){
        this.event_counter++;
        this.local_vector_clock.replace(this.port, this.event_counter);
        Message<Operation> msg = new Message<Operation>(this.port, op, this.local_vector_clock);
        this.assp.sendMessageToServers(msg);
    }


    /**
     * Getter for event_counter variable.
     *
     * @return int Event Counter.
     */
    public synchronized int getEvent_counter() {
        return this.event_counter;
    }
}
