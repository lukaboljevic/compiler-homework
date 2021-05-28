public class ForStatement extends Statement{

    AssignExpression firstAssign;
    AssignExpression secondAssing;
    Expression expression;
    CommandSequence commandSequence;

    public ForStatement(){
        this.kind = "FOR statement";
    }

}
