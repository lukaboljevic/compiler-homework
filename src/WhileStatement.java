public class WhileStatement extends Statement{

    Expression expression;
    CommandSequence commandSequence;

    public WhileStatement(){
        this.kind = "WHILE statement";
    }

}
