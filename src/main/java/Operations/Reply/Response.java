package Operations.Reply;

public class Response {
    private final ResponseType type;
    private final Object obj;

    public Response(ResponseMessages rms){
        this.type = ResponseType.MESSAGES;
        this.obj = rms;
    }

    public Response(ResponseTopics rts){
        this.type = ResponseType.TOPICS;
        this.obj = rts;
    }

    public Response(Confirm c){
        this.type = ResponseType.CONFIRM;
        this.obj = c;
    }

    public ResponseType getType() {
        return type;
    }

    public Object getObj() {
        return obj;
    }
}
