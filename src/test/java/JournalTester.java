import Middleware.Journal;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.HashMap;
import java.util.Map;

public class JournalTester {
    public static void main(String[] args) {
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
}
