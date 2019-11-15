package Server;

import io.atomix.utils.net.Address;

import java.util.*;

class CausalDelivery {
    private int port;
    private int event_counter = 0;
    private Map<Integer,Integer> local_vector_clock = new HashMap<>();
    private List<Message> waiting_queue = new LinkedList<>();

    /**
     * Parameterized constructor that initializes an instance of CausalDelivery.
     *
     * @param p The port where the server will operate.
     * */
    public CausalDelivery(int p){
        this.port = p;
    }

    /**
     *
     * */
    public void receive(Message msg){

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

        // delivers to edge class
        //this.mt.write(Address.from(msg.port), msg.message);

        // Makes the routine N times until the state get consistent
        //while (checkPendingMessages()) ;
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
