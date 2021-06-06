public class StatementPrint extends Statement {

    Expression expression;

    public StatementPrint(Expression expression) {
        this.expression = expression;
        this.kind = StatementKind.PRINT_STATEMENT;
    }

}
