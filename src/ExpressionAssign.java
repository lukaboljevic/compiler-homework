public class ExpressionAssign extends Statement{

    String identifier;
    Expression expression;

    public ExpressionAssign(String identifier, Expression expression){
        this.identifier = identifier;
        this.expression = expression;
        this.kind = StatementKind.ASSIGN_EXPR;
    }

}
