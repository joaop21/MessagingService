package Application;

import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Journal {
    private SegmentedJournal<Object> j;
    private SegmentedJournalReader<Object> reader;
    private SegmentedJournalWriter<Object> writer;

    /**
     * Parameterized Constructor that initializes an instance of this object.
     *
     * @param file_name The name of the log file.
     * @param s Serializer that is used to write in log.
     * */
    public Journal(String file_name, Serializer s) {

        this.j = SegmentedJournal.builder()
                .withName(file_name)
                .withSerializer(s)
                .build();

        this.reader = j.openReader(0);
        this.writer = null;
    }

    /**
     * Method that closes pendent open descriptors of the log.
     * */
    public void close(){
        if ( this.reader != null) this.reader.close();
        if ( this.writer != null) this.writer.close();
        this.j.close();
    }

    /**
     * Method that gets the object that resides on a certain index from the log.
     *
     * @param index Integer that indicates where to start collecting objects.
     * @param maxindex Integer that indicates where to stop collecting objects.
     *
     * @return List<Object> Objects in the log.
     * */
    public synchronized List<Object> getIndexObject(int index, int maxindex){
        if(this.writer != null){
            this.writer.close();
            this.writer = null;
        }

        List<Object> res = new ArrayList<>();

        this.reader = j.openReader(index-1);
        int iterator = maxindex - index;
        while(this.reader.hasNext()) {
            res.add(this.reader.next().entry());
            iterator--;
            if(iterator == 0)
                break;
        }
        return res;
    }

    /**
     * Method that gets the last object from the log.
     *
     * @return Object Last object in the log.
     * */
    public synchronized Object getLastObject(){
        if(this.writer != null){
            this.writer.close();
            this.writer = null;
        }

        this.reader = j.openReader(0);
        Object last = null;
        while(this.reader.hasNext()) {
            last = this.reader.next().entry();
        }
        return last;
    }

    /**
     * Method that gets the all objects from the log.
     *
     * @return List<Object></Object> List of objects from the log.
     * */
    public synchronized List<Object> getObjectsLog(){
        if(this.writer != null){
            this.writer.close();
            this.writer = null;
        }

        this.reader = j.openReader(0);
        List<Object> result = new ArrayList<>();
        while(this.reader.hasNext()) {
            result.add(this.reader.next().entry());
        }
        return result;
    }

    /**
     * Method that writes an object to log.
     *
     * @param obj Object to be written.
     * */
    public synchronized void writeObject(Object obj){
        if(this.reader != null){
            this.reader.close();
            this.reader = null;
        }
        this.writer = this.j.writer();

        this.writer.append(obj);
        CompletableFuture.supplyAsync(()->{
                this.writer.flush();
                return null;
            });
    }
}
