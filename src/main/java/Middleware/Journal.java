package Middleware;

import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.storage.journal.SegmentedJournalReader;
import io.atomix.storage.journal.SegmentedJournalWriter;
import io.atomix.utils.serializer.Serializer;

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
     * Method that gets the last object from the log.
     *
     * @return Object Last object in the log.
     * */
    public Object getLastObject(){
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
     * Method thar writes an object to log.
     *
     * @param obj Object to be written.
     * */
    public void writeObject(Object obj){
        if(this.reader != null){
            this.reader.close();
            this.reader = null;
            this.writer = this.j.writer();
        }

        this.writer.append(obj);
        writer.flush();
        CompletableFuture.supplyAsync(()->{
                this.writer.flush();
                return null;
            }).thenRun(()->{
                    this.writer.close();
                });
    }
}
