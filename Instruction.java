public class Instruction {

    private String opcode;
    private String dest;
    private String source;
    private String target;
    private String function;

    // Constructor
    public Instruction(String opcode, String dest, String source, String target, String function) {
        this.opcode = opcode;
        this.dest = dest;
        this.source = source;
        this.target = target;
        this.function = function;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public String getOpcode() {
        return opcode;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getDest() {
        return dest;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
    public void setFunction(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }
}