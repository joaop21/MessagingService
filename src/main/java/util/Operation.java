package util;

public class Operation {
    private OperationType type;
    private String[] args;

    public Operation(OperationType type, String[] args) {
        this.type = type;
        this.args = args;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}