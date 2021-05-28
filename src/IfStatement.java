public class IfStatement extends Statement{

    Expression expr;
    CommandSequence commandSequence;
    EndIfStatement endIfStatement;

    public IfStatement(){
        this.kind = "IF statement";
    }

}
