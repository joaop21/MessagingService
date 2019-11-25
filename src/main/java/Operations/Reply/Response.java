package Operations.Reply;

public class Response {
    private ResponseType type;
    private Confirm c;
    private ResponseMessages rms;
    private ResponseTopics rts;

    public Response(ResponseType t, Object o){
        this.type = t;
        switch (this.type){
            case MESSAGES:
                this.rms = (ResponseMessages) o;
                this.c = null;
                this.rts = null;
                break;
            case TOPICS:
                this.rts = (ResponseTopics) o;
                this.rms = null;
                this.c = null;
                break;
            case CONFIRM:
                this.c = (Confirm) o;
                this.rts = null;
                this.rms = null;
                break;
        }
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public Confirm getC() {
        return c;
    }

    public void setC(Confirm c) {
        this.c = c;
    }

    public ResponseMessages getRms() {
        return rms;
    }

    public void setRms(ResponseMessages rms) {
        this.rms = rms;
    }

    public ResponseTopics getRts() {
        return rts;
    }

    public void setRts(ResponseTopics rts) {
        this.rts = rts;
    }
}
