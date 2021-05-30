public class StatementIf extends Statement{

    Expression expression;
    CommandSequence commandSequence;
    StatementEndIf endIfStatement;

    public StatementIf(Expression expr, CommandSequence cs, StatementEndIf endIf){
        this.expression = expr;
        this.commandSequence = cs;
        this.endIfStatement = endIf;
        this.kind = StatementKind.IF_STATEMENT;
    }

}
