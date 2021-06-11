public class ExpressionAssignEnd extends Statement {

    Expression first;
    Expression second;

    ExpressionAssignEnd(Expression first, Expression second) {
        this.first = first;
        this.second = second;
        this.kind = StatementKind.ASSIGN_EXPR_END;
    }
}
