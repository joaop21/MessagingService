package Middleware;

import java.util.*;

class CausalDelivery {
    private int port;
    private int event_counter = 0;
    private Map<Integer,Integer> local_vector_clock = new HashMap<>();
    private List<Message> waiting_queue = new LinkedList<>();
    private AsynchronousProcess asp;
    private ServerMiddleware midd;

    /**
     * Parameterized constructor that initializes an instance of CausalDelivery.
     *
     * @param p The port where the server will operate.
     * @param midd The Facade that need to be accessed from this class.
     * */
    CausalDelivery(int p, ServerMiddleware midd, int[] net){
        this.port = p;
        this.midd = midd;

        // initialize local_vector_clock
        for (int value : net)
            this.local_vector_clock.put(value, 0);

        // Starting AsynchronousProcess thread
        this.asp = new AsynchronousProcess(this.port, this.midd,this, this.local_vector_clock.keySet().toArray());
        this.midd.setAsp(this.asp);
        new Thread(this.asp).start();
    }

    /**
     * Method that is responsable for testing causal delivery rules and act depending
     *   on the situation.
     *
     * @param msg Message that contains info to analyse.
     * */
    synchronized void receive(Message msg){

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
        this.local_vector_clock.replace(msg.port, (int) msg.sender_vector_clock.get(msg.port));

        // delivers to facade class
        this.midd.addServerMessage(msg);

        // Makes the routine N times until the state get consistent
        // Could exist messages on the end of the queue that unlock messages on the first places
        // it's necessary run N time
        while(checkPendingMessages());
    }

    /**
     * Method that increments the event counter and sends the massage to other servers.
     *
     * @param obj Object to be passed to the other servers.
     * */
    synchronized void sendMessageToServers(Object obj){
        this.event_counter++;
        this.local_vector_clock.replace(this.port, this.event_counter);
        this.asp.sendMessageToServers(new Message(this.port, obj, this.local_vector_clock));
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
            Message msg = this.waiting_queue.get(i);

            if(firstCausalRule(msg) && secondCausalRule(msg)){
                // When passes the Causal tests, change the clock
                this.local_vector_clock.replace(msg.port, (int) msg.sender_vector_clock.get(msg.port));
                // delivers to middleware class
                this.midd.addServerMessage(msg);
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
    private boolean firstCausalRule(Message msg){
        int local_value = this.local_vector_clock.get(msg.port);
        int sender_value = (int) msg.sender_vector_clock.get(msg.port);
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
    private boolean secondCausalRule(Message msg){
        Set<Map.Entry<Integer,Integer>> k_values = msg.sender_vector_clock.entrySet();

        for (Map.Entry<Integer, Integer> entry : k_values) {
            // case is not the sender port && the second rule fails
            if ((msg.port != entry.getKey()) && (this.local_vector_clock.get(entry.getKey()) < entry.getValue()))
                return false;
        }
        return true;
    }

}
