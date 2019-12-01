package Operations;

import Operations.Post.Post;
import Operations.Reply.Response;
import Operations.Request.Request;

public class Operation {
    private OperationType type;
    private Object op;

    public Operation(Request r){
        this.type = OperationType.REQUEST;
        this.op = r;
    }

    public Operation(Post p){
        this.type = OperationType.POST;
        this.op = p;
    }

    public Operation(Response resp){
        this.type = OperationType.RESPONSE;
        this.op = resp;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public Object getOp() {
        return op;
    }

    public void setOp(Object op) {
        this.op = op;
    }
}