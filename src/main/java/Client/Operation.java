public class Operation {
    private Messages type;
    private String[] args;

    public Operation(Messages type, String[] args) {
        this.type = type;
        this.args = args;
    }

    public Messages getType() {
        return type;
    }

    public void setType(Messages type) {
        this.type = type;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}