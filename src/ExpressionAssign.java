public class ExpressionAssign extends Statement {

    String identifier;
    Expression expression;
    ExpressionAssignEnd end;

    public ExpressionAssign(String identifier, Expression expression, ExpressionAssignEnd end) {
        this.identifier = identifier;
        this.expression = expression;
        this.end = end;
        this.kind = StatementKind.ASSIGN_EXPR;
    }

}
