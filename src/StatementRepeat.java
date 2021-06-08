public class StatementRepeat extends Statement {

    CommandSequence commandSequence;
    Expression expression;

    public StatementRepeat(CommandSequence cs, Expression expr) {
        this.commandSequence = cs;
        this.expression = expr;
        this.kind = StatementKind.REPEAT_STATEMENT;
    }

}
