package Operations.Request;

public class Request {
    private RequestType type;
    private RequestMessages rms;
    private RequestTopics rts;

    public Request(RequestType t, Object o){
        this.type = t;
        switch (this.type){
            case MESSAGES:
                this.rms = (RequestMessages) o;
                this.rts = null;
                break;
            case TOPICS:
                this.rts = (RequestTopics) o;
                this.rms = null;
                break;
        }
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public RequestMessages getRms() {
        return rms;
    }

    public void setRms(RequestMessages rms) {
        this.rms = rms;
    }

    public RequestTopics getRts() {
        return rts;
    }

    public void setRts(RequestTopics rts) {
        this.rts = rts;
    }
}
