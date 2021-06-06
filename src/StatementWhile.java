public class StatementWhile extends Statement {

    Expression expression;
    CommandSequence commandSequence;

    public StatementWhile(Expression expression, CommandSequence cs) {
        this.expression = expression;
        this.commandSequence = cs;
        this.kind = StatementKind.WHILE_STATEMENT;
    }

}
