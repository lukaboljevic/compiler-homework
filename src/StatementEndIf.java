public class StatementEndIf extends Statement {

    CommandSequence commandSequence;

    public StatementEndIf(CommandSequence cs) {
        this.commandSequence = cs;
        this.kind = StatementKind.END_IF_STATEMENT;
    }

}
