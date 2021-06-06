public class StatementFor extends Statement {

    ExpressionAssign first;
    ExpressionAssign second;
    Expression expression;
    CommandSequence commandSequence;

    public StatementFor(ExpressionAssign first, Expression expr, ExpressionAssign second, CommandSequence cs) {
        this.first = first;
        this.expression = expr;
        this.second = second;
        this.commandSequence = cs;
        this.kind = StatementKind.FOR_STATEMENT;
    }

}
