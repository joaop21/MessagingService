package Operations.Request;

public class Request {
    private final RequestType type;
    private final Object obj;

    public Request(RequestMessages rms){
        this.type = RequestType.MESSAGES;
        this.obj = rms;
    }

    public Request(RequestTopics rts){
        this.type = RequestType.TOPICS;
        this.obj = rts;
    }

    public RequestType getType() {
        return type;
    }

    public Object getObj() {
        return obj;
    }
}
